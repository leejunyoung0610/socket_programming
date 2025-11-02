package server.core;

import server.config.ServerConfig;
import server.filter.Filter;
import server.filter.FilterChain;
import server.http.ConnectionPolicy;
import server.http.HttpParseException;
import server.http.HttpRequest;
import server.http.HttpRequestParser;
import server.http.HttpResponse;
import server.http.HttpResponseWriter;
import server.route.Router;
import server.util.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 단일 TCP 연결에 대한 HTTP 요청/응답 처리를 담당하는 워커.
 * keep-alive를 고려하여 하나의 소켓에서 여러 요청을 순차적으로 처리한다.
 */
public final class ConnectionWorker implements Runnable {

    private final Socket socket;
    private final Router router;
    private final List<Filter> filters;
    private final HttpRequestParser parser;
    private final ConnectionPolicy policy;
    private final HttpResponseWriter writer;

    public ConnectionWorker(Socket socket, Router router, List<Filter> filters) {
        this.socket = socket;
        this.router = router;
        this.filters = filters;
        this.parser = new HttpRequestParser();
        this.policy = new ConnectionPolicy();
        this.writer = new HttpResponseWriter();
    }

    @Override
    public void run() {
        try (Socket s = socket) {
            // 읽기 타임아웃을 걸어 유휴 연결이 무한정 대기하지 않도록 한다.
            s.setSoTimeout(ServerConfig.SOCKET_TIMEOUT_MILLIS);
            BufferedInputStream in = new BufferedInputStream(s.getInputStream());
            OutputStream out = s.getOutputStream();
            int handledRequests = 0;
            boolean keepAlive;
            do {
                HttpRequest request;
                try {
                    // 요청 라인/헤더/바디를 순서대로 파싱한다.
                    request = parser.parse(in);
                } catch (SocketTimeoutException e) {
                    Logger.warn("Socket timeout from " + s.getRemoteSocketAddress());
                    break;
                } catch (HttpParseException e) {
                    Logger.warn("Bad request from " + s.getRemoteSocketAddress() + ": " + e.getMessage());
                    // 파싱 실패가 났을 때 이미 클라이언트가 연결을 끊었으면 오류 응답을 보내지 않는다.
                    if (!s.isOutputShutdown() && !s.isClosed()) {
                        sendError(out, 400, "Bad Request", e.getMessage());
                    } else {
                        Logger.warn("클라이언트가 연결을 끊어 오류 응답을 생략합니다.");
                    }
                    break;
                }
                if (request == null) {
                    // 클라이언트가 연결을 종료한 경우 null 반환으로 루프를 마친다.
                    break;
                }

                HttpResponse response;
                try {
                    FilterChain chain = new FilterChain(filters, router);
                    response = chain.doFilter(request);
                } catch (Exception e) {
                    Logger.error("Handler failure", e);
                    response = HttpResponse.builder(500, "Internal Server Error")
                            .header("Content-Type", "text/plain; charset=UTF-8")
                            .body("Internal Server Error".getBytes(StandardCharsets.UTF_8))
                            .build();
                }
                handledRequests++;
                keepAlive = policy.shouldKeepAlive(request, handledRequests);
                // 기존 응답 객체를 기반으로 keep-alive 헤더 등을 보강한다.
                HttpResponse.Builder builder = HttpResponse.builder(response.statusCode(), response.reasonPhrase());
                response.headers().forEach(builder::header);
                builder.body(response.body());
                policy.applyResponseHeaders(builder, keepAlive);
                HttpResponse finalResponse = builder.build();
                boolean includeBody = !"HEAD".equalsIgnoreCase(request.method());
                writer.write(out, finalResponse, includeBody);
            } while (keepAlive);
        } catch (IOException e) {
            Logger.error("IO error on connection", e);
        }
    }

    private void sendError(OutputStream out, int status, String reason, String message) {
        try {
            HttpResponse response = HttpResponse.builder(status, reason)
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(message.getBytes(StandardCharsets.UTF_8))
                    .build();
            writer.write(out, response, true);
        } catch (IOException ioe) {
            if (ioe instanceof SocketException) {
                Logger.warn("오류 응답을 보내기 전에 클라이언트 연결이 종료되었습니다: " + ioe.getMessage());
            } else {
                Logger.error("Failed to send error response", ioe);
            }
        }
    }
}
