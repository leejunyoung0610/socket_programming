package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.AuthService;
import server.util.CookieUtil;
import server.util.Logger;

import static server.http.ErrorResponses.unauthorizedAlert;

import java.util.Locale;
import java.util.Set;

/**
 * 세션 쿠키를 검사하여 인증되지 않은 사용자의 접근을 제한하는 필터.
 * 화이트리스트 경로는 로그인 없이 통과시키고, 그 외 경로는 세션이 유효해야 한다.
 */
public final class SessionFilter implements Filter {
    private final AuthService authService;
    private final Set<String> publicGetPaths;
    private final Set<String> publicPostPaths;
    private final String loginPage;

    public SessionFilter(Set<String> publicGetPaths, Set<String> publicPostPaths, String loginPage) {
        this.authService = new AuthService();
        this.publicGetPaths = publicGetPaths;
        this.publicPostPaths = publicPostPaths;
        this.loginPage = loginPage;
    }

    @Override
    public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
        String method = req.method().toUpperCase(Locale.ROOT);
        String path = normalize(req.path());

        if (isPublic(method, path)) {
            return chain.doFilter(req);
        }

        String sessionId = CookieUtil.getSessionId(req);
        boolean valid = sessionId != null && authService.validateSession(sessionId).isPresent();
        if (!valid) {
            Logger.warn("Unauthorized request: " + method + " " + path);
            return unauthorizedAlert(req, "로그인이 필요합니다.", loginPage);
        }

        return chain.doFilter(req);
    }

    private boolean isPublic(String method, String path) {
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return publicGetPaths.contains(path);
        }
        if ("POST".equals(method)) {
            return publicPostPaths.contains(path);
        }
        return false;
    }

    private String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path;
    }
}
