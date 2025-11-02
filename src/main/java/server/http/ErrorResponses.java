package server.http;

import java.nio.charset.StandardCharsets;

/**
 * 필터나 예외 처리에서 재사용하는 공통 에러 응답 생성기.
 */
public final class ErrorResponses {
    private ErrorResponses() {}

    public static HttpResponse badRequestAlert(HttpRequest req, String message, String home) {
        return build(400, "Bad Request", message, home);
    }

    public static HttpResponse forbiddenAlert(HttpRequest req, String message, String home) {
        return build(403, "Forbidden", message, home);
    }

    public static HttpResponse payloadTooLargeAlert(HttpRequest req, String message, String home) {
        return build(413, "Payload Too Large", message, home);
    }

    public static HttpResponse unsupportedMediaTypeAlert(HttpRequest req, String message, String home) {
        return build(415, "Unsupported Media Type", message, home);
    }

    public static HttpResponse unauthorizedAlert(HttpRequest req, String message, String home) {
        return build(401, "Unauthorized", message, home);
    }

    public static HttpResponse serverErrorAlert(HttpRequest req, String message, String home) {
        return build(500, "Internal Server Error", message, home);
    }

    private static HttpResponse build(int status, String reason, String message, String home) {
        String body = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head><meta charset=\"UTF-8\"><title>" + reason + "</title></head>\n" +
                "<body style=\"font-family:Arial,sans-serif;margin:40px;\">" +
                "<h1>" + reason + "</h1>" +
                "<p>" + escape(message) + "</p>" +
                "<p><a href=\"" + home + "\">홈으로 돌아가기</a></p>" +
                "</body></html>";
        return HttpResponse.builder(status, reason)
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(body.getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
