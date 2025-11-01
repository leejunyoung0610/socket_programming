
package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.util.Logger;

/**
 * 확장된 라우터 - 경로별로 다른 핸들러를 등록하고 라우팅
 */
public final class ExtendedRouter {
    private final Handler staticFileHandler;
    private final Map<String, Handler> routeHandlers;

    public ExtendedRouter(Handler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;
        this.routeHandlers = new HashMap<>();
    }

    /**
     * 특정 경로에 핸들러 등록
     */
    public void addRoute(String path, Handler handler) {
        routeHandlers.put(path, handler);
        Logger.info("Route registered: " + path + " -> " + handler.getClass().getSimpleName());
    }

    /**
     * 요청 라우팅
     */
    public HttpResponse route(HttpRequest request) throws IOException {
        String target = request.target();
        String method = request.method();
        
        // 쿼리 스트링 제거
        String pathPart = target.split("\\?", 2)[0];
        
        // 등록된 라우트 핸들러 확인
        Handler routeHandler = routeHandlers.get(pathPart);
        if (routeHandler != null) {
            Logger.info("Routing " + method + " " + pathPart + " to " + routeHandler.getClass().getSimpleName());
            return routeHandler.handle(request);
        }

        // GET/HEAD 요청은 정적 파일 핸들러로
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return staticFileHandler.handle(request);
        }

        // 지원하지 않는 메소드/경로
        return methodNotAllowed();
    }

    private HttpResponse methodNotAllowed() {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", "GET, HEAD, POST")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
