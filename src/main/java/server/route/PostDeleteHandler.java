package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.PostService;
import server.util.JsonUtil;

/**
 * 게시글 삭제 요청을 처리하는 핸들러.
 */
public final class PostDeleteHandler implements Handler {
    private final PostService postService;

    public PostDeleteHandler(PostService postService) {
        this.postService = postService;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        if (!"POST".equals(request.method())) {
            return methodNotAllowed();
        }

        String contentType = request.header("content-type");
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            return badRequest("Content-Type must be application/json");
        }

        String body = new String(request.body(), StandardCharsets.UTF_8);
        Map<String, String> data = JsonUtil.parseSimpleJson(body);

        String filename = data.get("filename");
        if (filename == null || filename.trim().isEmpty()) {
            return badRequest("삭제할 파일명을 입력해 주세요.");
        }

        boolean deleted = postService.deletePost(filename);
        if (!deleted) {
            return notFound();
        }

        return HttpResponse.builder(200, "OK")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(true, "게시글이 삭제되었습니다.").getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse methodNotAllowed() {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", "POST")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse badRequest(String message) {
        return HttpResponse.builder(400, "Bad Request")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(false, message).getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse notFound() {
        return HttpResponse.builder(404, "Not Found")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(false, "삭제할 게시글을 찾을 수 없습니다.").getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
