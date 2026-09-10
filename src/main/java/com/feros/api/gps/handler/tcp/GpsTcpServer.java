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
import java.time.Instant;
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
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII), true)) {

            // First line must be a login packet
            String loginLine = reader.readLine();
            if (loginLine == null || !loginLine.startsWith("$LGN")) {
                log.warn("GPS TCP: first packet not $LGN from {} — got: {}", remote, loginLine);
                return;
            }

            String imei = extractImei(loginLine);
            if (imei == null) {
                log.warn("GPS TCP: cannot extract IMEI from login: {}", loginLine);
                return;
            }

            GpsDevice device = deviceRepo.findActiveByDeviceIdentifierEager(imei).orElse(null);
            if (device == null) {
                log.warn("GPS TCP: unknown IMEI {} from {}", imei, remote);
                return;
            }

            GpsPacketParser parser = parserRegistry.getParser(device.getModel().getParserKey()).orElse(null);
            if (parser == null) {
                log.error("GPS TCP: no parser for key {} — IMEI {}", device.getModel().getParserKey(), imei);
                return;
            }

            // ACK the login so device starts sending tracking packets
            writer.println("ACK");

            TcpSessionContext ctx = TcpSessionContext.builder()
                    .device(device)
                    .parser(parser)
                    .remoteAddress(remote)
                    .connectedAt(Instant.now())
                    .build();

            log.info("GPS session started — {} IMEI {}", device.getVehicle().getRegistrationNumber(), imei);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                final String frame = line;
                parser.parse(ctx.getDevice(), frame).ifPresent(ping -> {
                    odometerService.accumulate(ping);  // uses liveStore.getLatest BEFORE update
                    routeService.maybeRecord(ping);     // uses liveStore.getLastRoutePoint BEFORE update
                    liveStore.update(ping);             // update last known position
                    persistenceService.save(ping);      // persist to gps_pings
                });
            }

            log.info("GPS session ended — IMEI {}", imei);

        } catch (SocketTimeoutException e) {
            log.info("GPS TCP session timeout — {}", remote);
        } catch (Exception e) {
            log.warn("GPS TCP session error from {}: {}", remote, e.getMessage());
        }
    }

    // $LGN,vehicleReg,IMEI,firmware,...*checksum  →  field[2] = IMEI
    private String extractImei(String loginLine) {
        try {
            int star = loginLine.lastIndexOf('*');
            String stripped = star > 0 ? loginLine.substring(0, star) : loginLine;
            String[] fields = stripped.split(",");
            return fields.length >= 3 ? fields[2].trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
