package server.service;

import java.util.Optional;

import server.model.Session;
import server.model.User;

/**
 * 사용자 인증 통합 서비스
 */
public final class AuthService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.sessionManager = new SessionManager();
    }

    /**
     * 사용자 회원가입
     */
    public RegisterResult register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new RegisterResult(false, "아이디를 입력해주세요.");
        }

        if (password == null || password.length() < 6) {
            return new RegisterResult(false, "비밀번호는 최소 6자 이상이어야 합니다.");
        }

        if (userRepository.userExists(username)) {
            return new RegisterResult(false, "이미 존재하는 아이디입니다.");
        }

        boolean success = userRepository.registerUser(username, password);
        if (success) {
            return new RegisterResult(true, "회원가입이 완료되었습니다.");
        } else {
            return new RegisterResult(false, "회원가입 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 로그인
     */
    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return new LoginResult(false, null, "아이디와 비밀번호를 입력해주세요.");
        }

        boolean authenticated = userRepository.authenticateUser(username, password);
        if (authenticated) {
            Session session = sessionManager.createSession(username);
            return new LoginResult(true, session, "로그인 성공");
        } else {
            return new LoginResult(false, null, "아이디 또는 비밀번호가 잘못되었습니다.");
        }
    }

    /**
     * 세션 검증
     */
    public Optional<String> validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> sessionOpt = sessionManager.getValidSession(sessionId);
        return sessionOpt.map(Session::getUsername);
    }

    /**
     * 로그아웃
     */
    public void logout(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            sessionManager.deleteSession(sessionId);
        }
    }

    /**
     * 사용자 정보 조회
     */
    public Optional<User> getUser(String username) {
        return userRepository.loadUser(username);
    }

    /**
     * 만료된 세션 정리
     */
    public void cleanupExpiredSessions() {
        sessionManager.cleanupExpiredSessions();
    }

    /**
     * 회원가입 결과
     */
    public static class RegisterResult {
        private final boolean success;
        private final String message;

        public RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 로그인 결과
     */
    public static class LoginResult {
        private final boolean success;
        private final Session session;
        private final String message;

        public LoginResult(boolean success, Session session, String message) {
            this.success = success;
            this.session = session;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public Session getSession() {
            return session;
        }

        public String getMessage() {
            return message;
        }
    }
}