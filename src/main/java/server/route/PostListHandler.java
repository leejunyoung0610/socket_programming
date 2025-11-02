package server.route;

import java.nio.charset.StandardCharsets;
import java.util.List;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.PostService;
import server.util.JsonUtil;

/**
 * 게시글 파일 목록을 반환하는 핸들러.
 */
public final class PostListHandler implements Handler {

    private final PostService postService;

    public PostListHandler(PostService postService) {
        this.postService = postService;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        if (!"POST".equals(request.method())) {
            return methodNotAllowed();
        }
        List<String> posts = postService.listPosts();
        String body = JsonUtil.createListResponse(true, "", posts);
        return HttpResponse.builder(200, "OK")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(body.getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse methodNotAllowed() {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", "POST")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
