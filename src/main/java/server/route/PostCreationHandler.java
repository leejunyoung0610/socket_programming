package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.PostService;
import server.util.JsonUtil;

/**
 * 게시글 생성 요청을 처리하는 핸들러.
 */
public final class PostCreationHandler implements Handler {
    private final PostService postService;

    public PostCreationHandler(PostService postService) {
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

        String title = data.get("title");
        String content = data.get("content");
        String author = data.get("author");

        if (!postService.createPost(title, content, author)) {
            return badRequest("제목과 내용을 모두 입력해 주세요.");
        }

        return HttpResponse.builder(201, "Created")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(true, "게시글이 등록되었습니다.").getBytes(StandardCharsets.UTF_8))
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
}
