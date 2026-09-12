package com.feros.api.gps.handler.tcp;

import com.feros.api.entity.GpsDevice;
import com.feros.api.enums.GpsConnectionType;
import com.feros.api.gps.GpsLiveStore;
import com.feros.api.gps.GpsOdometerService;
import com.feros.api.gps.GpsPingPersistenceService;
import com.feros.api.gps.GpsRouteService;
import com.feros.api.gps.handler.GpsConnectionHandler;
import com.feros.api.gps.parser.GpsPacketParser;
import com.feros.api.gps.parser.GpsParserRegistry;
import com.feros.api.repository.GpsDeviceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class GpsTcpServer implements GpsConnectionHandler {

    @Value("${gps.server.host}")      private String  serverHost;
    @Value("${gps.tcp.enabled:true}") private boolean enabled;
    @Value("${gps.tcp.port:2024}")    private int     port;
    @Value("${gps.tcp.read-timeout-seconds:300}") private int readTimeoutSeconds;

    private final GpsParserRegistry        parserRegistry;
    private final GpsDeviceRepository      deviceRepo;
    private final GpsPingPersistenceService persistenceService;
    private final GpsOdometerService       odometerService;
    private final GpsRouteService          routeService;
    private final GpsLiveStore             liveStore;

    private ServerSocket  serverSocket;
    private ExecutorService threadPool;
    private final Set<String> staleImeis = ConcurrentHashMap.newKeySet();

    public void kickDevice(String imei) {
        staleImeis.add(imei);
    }

    public GpsTcpServer(GpsParserRegistry parserRegistry,
                        GpsDeviceRepository deviceRepo,
                        GpsPingPersistenceService persistenceService,
                        GpsOdometerService odometerService,
                        GpsRouteService routeService,
                        GpsLiveStore liveStore) {
        this.parserRegistry    = parserRegistry;
        this.deviceRepo        = deviceRepo;
        this.persistenceService = persistenceService;
        this.odometerService   = odometerService;
        this.routeService      = routeService;
        this.liveStore         = liveStore;
    }

    @Override
    public GpsConnectionType connectionType() {
        return GpsConnectionType.TCP;
    }

    @PostConstruct
    @Override
    public void start() {
        if (!enabled) {
            log.info("GPS TCP server disabled (gps.tcp.enabled=false)");
            return;
        }
        // ponytail: virtual threads (Java 21) — unmount while blocked on I/O, handles
        // hundreds of persistent GPS connections with negligible memory overhead
        threadPool = Executors.newVirtualThreadPerTaskExecutor();
        Thread acceptThread = new Thread(this::acceptLoop, "gps-tcp-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        log.info("GPS TCP server started on {}:{}", serverHost, port);
    }

    @PreDestroy
    @Override
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
        if (threadPool != null) threadPool.shutdownNow();
        log.info("GPS TCP server stopped");
    }

    private void acceptLoop() {
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                client.setSoTimeout(readTimeoutSeconds * 1000);
                threadPool.submit(() -> handleConnection(client));
            }
        } catch (IOException e) {
            if (serverSocket != null && !serverSocket.isClosed()) {
                log.error("GPS TCP accept loop error", e);
            }
        }
    }

    private void handleConnection(Socket socket) {
        String remote = socket.getRemoteSocketAddress().toString();
        log.info("GPS TCP connection from {}", remote);
        try (socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII))) {

            GpsDevice       device = null;
            GpsPacketParser parser = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                final String frame = line;

                // Identify device from first $PVT packet (IMEI at field[6])
                if (device == null) {
                    String imei = extractImei(frame);
                    if (imei == null) continue;

                    device = deviceRepo.findActiveByDeviceIdentifierEager(imei).orElse(null);
                    if (device == null) {
                        log.warn("GPS TCP: unknown IMEI {} from {}", imei, remote);
                        return;
                    }

                    parser = parserRegistry.getParser(device.getModel().getParserKey()).orElse(null);
                    if (parser == null) {
                        log.error("GPS TCP: no parser for key {} — IMEI {}", device.getModel().getParserKey(), imei);
                        return;
                    }

                    log.info("GPS session started — {} IMEI {}", device.getVehicle().getRegistrationNumber(), imei);
                }

                // Close session if device was reassigned — it will reconnect and re-lookup from DB
                if (staleImeis.remove(device.getDeviceIdentifier())) {
                    log.info("GPS TCP: device {} reassigned, closing session to force reconnect", device.getDeviceIdentifier());
                    return;
                }

                final GpsDevice       dev = device;
                final GpsPacketParser psr = parser;
                psr.parse(dev, frame).ifPresent(ping -> {
                    odometerService.accumulate(ping);
                    routeService.maybeRecord(ping);
                    liveStore.update(ping);
                    persistenceService.save(ping);
                });
            }

            log.info("GPS session ended — {}", remote);

        } catch (SocketTimeoutException e) {
            log.info("GPS TCP session timeout — {}", remote);
        } catch (Exception e) {
            log.warn("GPS TCP session error from {}: {}", remote, e.getMessage());
        }
    }

    // $LGN,vehicleReg,IMEI,...      → field[2] = IMEI
    // $PVT,vendor,fw,...,L/H,IMEI  → field[6] = IMEI
    private String extractImei(String line) {
        try {
            int star = line.lastIndexOf('*');
            String stripped = star > 0 ? line.substring(0, star) : line;
            String[] f = stripped.split(",");
            if (line.startsWith("$LGN") && f.length > 2) return f[2].trim();
            if (line.startsWith("$PVT") && f.length > 6) return f[6].trim();
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
