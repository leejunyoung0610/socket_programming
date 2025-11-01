package server.http;

import server.config.ServerConfig;

/**
 * HTTP 버전과 Connection 헤더를 바탕으로 keep-alive 여부와 응답 헤더를 결정한다.
 */
public final class ConnectionPolicy {

    public boolean shouldKeepAlive(HttpRequest request, int requestsServed) {
        String version = request.version();
        String connectionHeader = request.header("connection");
        if ("HTTP/1.1".equals(version)) {
            if (connectionHeader != null && "close".equalsIgnoreCase(connectionHeader)) {
                return false;
            }
            return requestsServed < ServerConfig.KEEP_ALIVE_MAX_REQUESTS;
        }
        if ("HTTP/1.0".equals(version)) {
            return connectionHeader != null
                    && "keep-alive".equalsIgnoreCase(connectionHeader)
                    && requestsServed < ServerConfig.KEEP_ALIVE_MAX_REQUESTS;
        }
        return false;
    }

    public void applyResponseHeaders(HttpResponse.Builder builder, boolean keepAlive) {
        if (keepAlive) {
            // 지속 연결을 허용하면 Connection과 Keep-Alive 헤더를 내려준다.
            builder.header("Connection", "keep-alive");
            builder.header("Keep-Alive", "timeout=" + (ServerConfig.KEEP_ALIVE_TIMEOUT_MILLIS / 1000)
                    + ", max=" + ServerConfig.KEEP_ALIVE_MAX_REQUESTS);
        } else {
            builder.header("Connection", "close");
        }
    }
}
