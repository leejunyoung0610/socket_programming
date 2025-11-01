package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import server.http.HttpRequest;
import server.http.HttpResponse;

/**
 * 요청 메소드/경로에 따라 적절한 핸들러로 분기해 주는 단순 라우터.
 */
public final class Router {
    private final Handler staticFileHandler;
    private final Handler postHandler;
    private final Handler postListHandler;

    public Router(Handler staticFileHandler) {
        this(staticFileHandler, null, null);
    }

    public Router(Handler staticFileHandler, Handler postHandler) {
        this(staticFileHandler, postHandler, null);
    }

    public Router(Handler staticFileHandler, Handler postHandler, Handler postListHandler) {
        this.staticFileHandler = staticFileHandler;
        this.postHandler = postHandler;
        this.postListHandler = postListHandler;
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method();
        String path = request.target().split("\\?", 2)[0]; // 쿼리 파라미터 제거
        
        if ("GET".equals(method) || "HEAD".equals(method)) {
            // API 경로 체크
            if ("/api/content".equals(path) && postListHandler != null) {
                return postListHandler.handle(request);
            }
            return staticFileHandler.handle(request);
        }
        if ("POST".equals(method)) {
            if (postHandler != null) {
                return postHandler.handle(request);
            }
            return notAllowed("GET, HEAD");
        }
        return notAllowed(postHandler != null ? "GET, HEAD, POST" : "GET, HEAD");
    }

    private HttpResponse notAllowed(String allowHeader) {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", allowHeader)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
