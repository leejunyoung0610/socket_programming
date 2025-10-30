package server.http;

import server.config.ServerConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HttpResponse 객체를 실제 HTTP 응답 포맷(상태라인/헤더/본문)으로 직렬화한다.
 */
public final class HttpResponseWriter {

    public void write(OutputStream out, HttpResponse response, boolean includeBody) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>(response.headers());
        // Date, Server 헤더가 없다면 기본 값을 채워 넣는다.
        headers.putIfAbsent("Date", DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(java.time.ZoneOffset.UTC)));
        headers.putIfAbsent("Server", ServerConfig.SERVER_NAME);

        // HEAD 같은 경우 본문 전송을 생략할 수 있도록 플래그로 분리한다.
        byte[] body = includeBody ? response.body() : new byte[0];
        headers.put("Content-Length", Integer.toString(body.length));

        String statusLine = "HTTP/1.1 " + response.statusCode() + " " + response.reasonPhrase() + "\r\n";
        out.write(statusLine.getBytes(StandardCharsets.US_ASCII));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.US_ASCII));
        }
        out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        if (includeBody && body.length > 0) {
            out.write(body);
        }
        out.flush();
    }
}
