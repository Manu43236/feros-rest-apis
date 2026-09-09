package com.feros.api.gps.handler.tcp;

import com.feros.api.enums.GpsConnectionType;
import com.feros.api.gps.handler.GpsConnectionHandler;
import com.feros.api.gps.parser.GpsParserRegistry;
import com.feros.api.repository.GpsDeviceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class GpsTcpServer implements GpsConnectionHandler {

    @Value("${gps.server.host}")
    private String serverHost;

    @Value("${gps.tcp.enabled:true}")
    private boolean enabled;

    @Value("${gps.tcp.port:2024}")
    private int port;

    @Value("${gps.tcp.thread-pool-size:50}")
    private int threadPoolSize;

    @Value("${gps.tcp.read-timeout-seconds:300}")
    private int readTimeoutSeconds;

    private final GpsParserRegistry parserRegistry;
    private final GpsDeviceRepository deviceRepo;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    public GpsTcpServer(GpsParserRegistry parserRegistry, GpsDeviceRepository deviceRepo) {
        this.parserRegistry = parserRegistry;
        this.deviceRepo = deviceRepo;
    }

    @Override
    public GpsConnectionType connectionType() {
        return GpsConnectionType.TCP;
    }

    @PostConstruct
    @Override
    public void start() {
        if (!enabled) {
            log.info("GPS TCP server is disabled (gps.tcp.enabled=false)");
            return;
        }
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        Thread acceptThread = new Thread(this::acceptLoop, "gps-tcp-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        log.info("GPS TCP server started — {}:{}", serverHost, port);
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
                log.error("GPS TCP server accept loop error", e);
            }
        }
    }

    private void handleConnection(Socket socket) {
        String remote = socket.getRemoteSocketAddress().toString();
        log.info("GPS TCP connection from {}", remote);
        try (socket) {
            // TODO: implement in Phase 3 Stage 1 once real packet format is confirmed
            // 1. Read login packet → extract IMEI → look up GpsDevice by deviceIdentifier
            // 2. Resolve parserKey from device.model → look up parser in GpsParserRegistry
            // 3. Build TcpSessionContext
            // 4. Read loop: BufferedReader.readLine() → one frame per line
            //    - Skip empty lines
            //    - Route frame to parser.parse(device, rawFrame)
            //    - On successful parse: persist GpsPing, update device.lastPingAt
        } catch (Exception e) {
            log.warn("GPS TCP session error from {}: {}", remote, e.getMessage());
        }
    }
}
