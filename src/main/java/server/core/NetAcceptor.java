package server.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import server.config.ServerConfig;
import server.filter.Filter;
import server.route.Router;
import server.util.Logger;

/**
 * 클라이언트 연결을 받아 ConnectionWorker에 위임하는 구성 요소.
 * ServerSocket과 워커 스레드 풀을 관리하며, start-stop 라이프사이클을 책임진다.
 */
public final class NetAcceptor implements Closeable {
    private final Router router;
    private final List<Filter> filters;
    private volatile boolean running;
    private ExecutorService executor;
    private ServerSocket serverSocket;

    public NetAcceptor(Router router, List<Filter> filters) {
        this.router = router;
        this.filters = List.copyOf(filters);
    }

    public void start() throws IOException {
        // 다중 호출을 방지하기 위해 락으로 상태를 확인한다.
        synchronized (this) {
            if (running) {
                return;
            }
            executor = Executors.newFixedThreadPool(ServerConfig.WORKER_THREADS);
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ServerConfig.PORT), ServerConfig.ACCEPT_BACKLOG);
            serverSocket.setSoTimeout(1000);
            running = true;
        }
        Logger.info("Server listening on port " + ServerConfig.PORT);
        try {
            while (isRunning()) {
                try {
                    // 새 연결을 수락하면 워커에게 처리하도록 맡긴다.
                    Socket socket = serverSocket.accept();
                    executor.execute(new ConnectionWorker(socket, router, filters));
                } catch (SocketTimeoutException e) {
                    // 1초마다 깨어나서 running 플래그를 점검한다.
                } catch (IOException e) {
                    if (isRunning()) {
                        Logger.error("Accept failed", e);
                    }
                }
            }
        } finally {
            cleanup();
        }
    }

    private boolean isRunning() {
        return running;
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    public void stop() throws IOException {
        synchronized (this) {
            if (!running) {
                return;
            }
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
        cleanup();
        Logger.info("Server stopped");
    }

    private void cleanup() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
            executor = null;
        }
        serverSocket = null;
    }
}
