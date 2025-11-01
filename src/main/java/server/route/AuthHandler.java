package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.AuthService;
import server.util.CookieUtil;
import server.util.JsonUtil;
import server.util.Logger;

/**
 * 로그인 및 회원가입 요청을 처리하는 핸들러
 */
public final class AuthHandler implements Handler {
    private final AuthService authService;

    public AuthHandler() {
        this.authService = new AuthService();
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        String target = request.target();
        String method = request.method();

        if (!"POST".equals(method)) {
            return methodNotAllowed();
        }

        if ("/login".equals(target)) {
            return handleLogin(request);
        } else if ("/register".equals(target)) {
            return handleRegister(request);
        } else if ("/logout".equals(target)) {
            return handleLogout(request);
        }

        return notFound();
    }

    /**
     * 로그인 처리
     */
    private HttpResponse handleLogin(HttpRequest request) {
        try {
            String contentType = request.header("content-type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                return badRequest("Content-Type must be application/json");
            }

            String body = new String(request.body(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonUtil.parseSimpleJson(body);
            
            String username = data.get("username");
            String password = data.get("password");

            AuthService.LoginResult result = authService.login(username, password);
            
            if (result.isSuccess()) {
                Logger.info("Login successful for user: " + username);
                
                HttpResponse.Builder builder = HttpResponse.builder(200, "OK")
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(JsonUtil.createResponse(true, result.getMessage()).getBytes(StandardCharsets.UTF_8));
                
                // 세션 쿠키 설정
                CookieUtil.setSessionCookie(builder, result.getSession().getSessionId());
                
                return builder.build();
            } else {
                Logger.warn("Login failed for user: " + username);
                return HttpResponse.builder(401, "Unauthorized")
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(JsonUtil.createResponse(false, result.getMessage()).getBytes(StandardCharsets.UTF_8))
                        .build();
            }
        } catch (Exception e) {
            Logger.error("Error processing login request", e);
            return internalServerError("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 회원가입 처리
     */
    private HttpResponse handleRegister(HttpRequest request) {
        try {
            String contentType = request.header("content-type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                return badRequest("Content-Type must be application/json");
            }

            String body = new String(request.body(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonUtil.parseSimpleJson(body);
            
            String username = data.get("username");
            String password = data.get("password");

            AuthService.RegisterResult result = authService.register(username, password);
            
            if (result.isSuccess()) {
                Logger.info("Registration successful for user: " + username);
                return HttpResponse.builder(201, "Created")
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(JsonUtil.createResponse(true, result.getMessage()).getBytes(StandardCharsets.UTF_8))
                        .build();
            } else {
                Logger.warn("Registration failed for user: " + username + " - " + result.getMessage());
                return HttpResponse.builder(400, "Bad Request")
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(JsonUtil.createResponse(false, result.getMessage()).getBytes(StandardCharsets.UTF_8))
                        .build();
            }
        } catch (Exception e) {
            Logger.error("Error processing registration request", e);
            return internalServerError("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그아웃 처리
     */
    private HttpResponse handleLogout(HttpRequest request) {
        try {
            String sessionId = CookieUtil.getSessionId(request);
            
            if (sessionId != null) {
                authService.logout(sessionId);
                Logger.info("Logout successful for session: " + sessionId);
            }

            HttpResponse.Builder builder = HttpResponse.builder(200, "OK")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(JsonUtil.createResponse(true, "로그아웃되었습니다.").getBytes(StandardCharsets.UTF_8));
            
            // 세션 쿠키 삭제
            CookieUtil.deleteSessionCookie(builder);
            
            return builder.build();
        } catch (Exception e) {
            Logger.error("Error processing logout request", e);
            return internalServerError("로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

    // Helper methods
    private HttpResponse methodNotAllowed() {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", "POST")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse notFound() {
        return HttpResponse.builder(404, "Not Found")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Not Found".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse badRequest(String message) {
        return HttpResponse.builder(400, "Bad Request")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(false, message).getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse internalServerError(String message) {
        return HttpResponse.builder(500, "Internal Server Error")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(false, message).getBytes(StandardCharsets.UTF_8))
                .build();
    }
}