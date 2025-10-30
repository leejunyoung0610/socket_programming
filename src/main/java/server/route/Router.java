package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import server.http.HttpRequest;
import server.http.HttpResponse;

/**
 * 요청 메소드/경로에 따라 적절한 핸들러로 분기해 주는 단순 라우터.
 * 로그인 기능 추가됨 (팀원 작업)
 */
public final class Router {
    private final Handler staticFileHandler;
    private final Handler postHandler;
    private final Handler authHandler; // 로그인 기능 추가

    public Router(Handler staticFileHandler) {
        this(staticFileHandler, null);
    }

    public Router(Handler staticFileHandler, Handler postHandler) {
        this(staticFileHandler, postHandler, null);
    }
    
    // 로그인 핸들러를 포함한 새 생성자 (팀원 작업)
    public Router(Handler staticFileHandler, Handler postHandler, Handler authHandler) {
        this.staticFileHandler = staticFileHandler;
        this.postHandler = postHandler;
        this.authHandler = authHandler;
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method();
        String target = request.target();
        
        // 로그인 관련 경로 처리 (팀원 작업)
        if ("POST".equals(method) && authHandler != null) {
            if ("/login".equals(target) || "/register".equals(target) || "/logout".equals(target)) {
                return authHandler.handle(request);
            }
        }
        
        // 기존 코드 유지
        if ("GET".equals(method) || "HEAD".equals(method)) {
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
