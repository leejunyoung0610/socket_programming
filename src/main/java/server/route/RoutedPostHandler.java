package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 경로별 POST 핸들러를 구성할 수 있는 래퍼 핸들러.
 * Router는 단일 POST 핸들러만 지원하므로, 이 클래스를 통해
 * 경로에 따라 다른 핸들러를 위임할 수 있다.
 */
public final class RoutedPostHandler implements Handler {
    private final Map<String, Handler> routes = new HashMap<>();
    private final Handler fallback;

    public RoutedPostHandler(Handler fallback) {
        this.fallback = fallback;
    }

    /**
     * 지정한 경로에 POST 핸들러를 등록한다.
     */
    public void register(String path, Handler handler) {
        if (path == null || handler == null) {
            return;
        }
        routes.put(normalize(path), handler);
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        Handler handler = routes.get(normalize(request.target()));
        if (handler != null) {
            return handler.handle(request);
        }
        if (fallback != null) {
            return fallback.handle(request);
        }
        return notFound();
    }

    private String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String normalized = path;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        return normalized;
    }

    private HttpResponse notFound() {
        return HttpResponse.builder(404, "Not Found")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Not Found".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
