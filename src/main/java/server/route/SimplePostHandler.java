package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.util.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * POST 요청을 단순히 수신하고 내용을 그대로 돌려주는 핸들러.
 * Body는 UTF-8로 해석하며, 필요 시 Content-Type의 charset 값을 사용한다.
 */
public final class SimplePostHandler implements Handler {

    @Override
    public HttpResponse handle(HttpRequest request) {
        byte[] body = request.body();
        String contentType = request.header("content-type");
        Charset charset = extractCharset(contentType);
        String bodyText = new String(body, charset);

        StringBuilder responseText = new StringBuilder();
        responseText.append("POST 요청을 처리했습니다.\n");
        responseText.append("수신 시각: ").append(Instant.now()).append('\n');
        responseText.append("Content-Type: ").append(contentType != null ? contentType : "없음").append('\n');
        responseText.append("본문 길이: ").append(body.length).append(" byte\n\n");
        if (body.length > 0) {
            responseText.append(bodyText);
        } else {
            responseText.append("(본문이 비어 있습니다)");
        }

        Logger.info("POST body size=" + body.length + " bytes");

        return HttpResponse.builder(201, "Created")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(responseText.toString().getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private Charset extractCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        String lower = contentType.toLowerCase();
        int idx = lower.indexOf("charset=");
        if (idx == -1) {
            return StandardCharsets.UTF_8;
        }
        String charsetName = lower.substring(idx + 8).trim();
        int semi = charsetName.indexOf(';');
        if (semi != -1) {
            charsetName = charsetName.substring(0, semi).trim();
        }
        try {
            return Charset.forName(charsetName);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }
}
