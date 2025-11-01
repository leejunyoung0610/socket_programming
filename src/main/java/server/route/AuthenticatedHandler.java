package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.AuthService;
import server.util.CookieUtil;
import server.util.Logger;

/**
 * 인증이 필요한 요청을 처리하는 핸들러
 * 로그인이 필요한 페이지나 API에 대한 접근 제어
 */
public final class AuthenticatedHandler implements Handler {
    private final Handler delegateHandler;
    private final AuthService authService;

    public AuthenticatedHandler(Handler delegateHandler) {
        this.delegateHandler = delegateHandler;
        this.authService = new AuthService();
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        // 세션 검증
        String sessionId = CookieUtil.getSessionId(request);
        Optional<String> username = authService.validateSession(sessionId);
        
        if (username.isEmpty()) {
            Logger.warn("Unauthorized access attempt to: " + request.target());
            return unauthorized();
        }

        Logger.info("Authenticated request for user: " + username.get() + " to: " + request.target());
        
        // 인증 성공 시 실제 핸들러에 위임
        return delegateHandler.handle(request);
    }

    private HttpResponse unauthorized() {
        return HttpResponse.builder(401, "Unauthorized")
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(createUnauthorizedPage().getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private String createUnauthorizedPage() {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>로그인 필요</title>
                    <style>
                        body { font-family: system-ui, -apple-system, sans-serif; background: #f7fafc; color: #111; margin: 0; padding: 0; }
                        .container { max-width: 500px; margin: 8rem auto; background: #fff; padding: 32px; border-radius: 8px; box-shadow: 0 6px 20px rgba(2,6,23,0.08); text-align: center; }
                        h1 { color: #dc2626; margin-bottom: 16px; }
                        p { margin-bottom: 24px; color: #6b7280; }
                        a { display: inline-block; padding: 12px 24px; background: #0369a1; color: white; text-decoration: none; border-radius: 6px; }
                        a:hover { background: #0284c7; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🔒 로그인이 필요합니다</h1>
                        <p>이 페이지에 접근하려면 먼저 로그인해야 합니다.</p>
                        <a href="/login.html">로그인 페이지로 이동</a>
                    </div>
                </body>
                </html>
                """;
    }
}