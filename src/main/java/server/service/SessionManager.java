package server.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import server.model.Session;
import server.util.Logger;

/**
 * 파일 기반 세션 관리자
 * sessions/ 디렉토리에 세션 정보를 파일로 저장
 */
public final class SessionManager {
    private static final Path SESSIONS_DIR = Paths.get("sessions");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long DEFAULT_SESSION_DURATION_MINUTES = 120; // 2시간

    public SessionManager() {
        try {
            Files.createDirectories(SESSIONS_DIR);
        } catch (IOException e) {
            Logger.error("Failed to create sessions directory", e);
        }
    }

    /**
     * 새 세션 생성
     */
    public Session createSession(String username) {
        return createSession(username, DEFAULT_SESSION_DURATION_MINUTES);
    }

    /**
     * 새 세션 생성 (지속시간 지정)
     */
    public Session createSession(String username, long durationMinutes) {
        Session session = new Session(username, durationMinutes);
        saveSession(session);
        Logger.info("Session created for user: " + username + ", sessionId: " + session.getSessionId());
        return session;
    }

    /**
     * 세션 검증 및 조회
     */
    public Optional<Session> getValidSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> sessionOpt = loadSession(sessionId);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        Session session = sessionOpt.get();
        if (session.isExpired()) {
            deleteSession(sessionId);
            return Optional.empty();
        }

        return Optional.of(session);
    }

    /**
     * 세션 삭제 (로그아웃)
     */
    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }

        Path sessionFile = SESSIONS_DIR.resolve(sessionId + ".txt");
        try {
            Files.deleteIfExists(sessionFile);
            Logger.info("Session deleted: " + sessionId);
        } catch (IOException e) {
            Logger.error("Failed to delete session: " + sessionId, e);
        }
    }

    /**
     * 만료된 세션들 정리
     */
    public void cleanupExpiredSessions() {
        try {
            Files.list(SESSIONS_DIR)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(path -> {
                    String sessionId = path.getFileName().toString().replace(".txt", "");
                    Optional<Session> sessionOpt = loadSession(sessionId);
                    if (sessionOpt.isPresent() && sessionOpt.get().isExpired()) {
                        deleteSession(sessionId);
                    }
                });
        } catch (IOException e) {
            Logger.error("Failed to cleanup expired sessions", e);
        }
    }

    /**
     * 세션 정보 로드
     */
    private Optional<Session> loadSession(String sessionId) {
        Path sessionFile = SESSIONS_DIR.resolve(sessionId + ".txt");
        if (!Files.exists(sessionFile)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(sessionFile);
            String[] lines = content.split("\n");
            
            if (lines.length < 4) {
                return Optional.empty();
            }

            String storedSessionId = lines[0].substring(10); // "sessionId:" 제거
            String username = lines[1].substring(9);         // "username:" 제거
            String createdAtStr = lines[2].substring(10);    // "createdAt:" 제거
            String expiresAtStr = lines[3].substring(10);    // "expiresAt:" 제거
            
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr, DATETIME_FORMAT);
            LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, DATETIME_FORMAT);

            return Optional.of(new Session(storedSessionId, username, createdAt, expiresAt));
        } catch (Exception e) {
            Logger.error("Failed to load session: " + sessionId, e);
            return Optional.empty();
        }
    }

    /**
     * 세션 정보 저장
     */
    private void saveSession(Session session) {
        Path sessionFile = SESSIONS_DIR.resolve(session.getSessionId() + ".txt");
        
        StringBuilder content = new StringBuilder();
        content.append("sessionId:").append(session.getSessionId()).append("\n");
        content.append("username:").append(session.getUsername()).append("\n");
        content.append("createdAt:").append(session.getCreatedAt().format(DATETIME_FORMAT)).append("\n");
        content.append("expiresAt:").append(session.getExpiresAt().format(DATETIME_FORMAT)).append("\n");

        try {
            Files.writeString(sessionFile, content.toString());
        } catch (IOException e) {
            Logger.error("Failed to save session: " + session.getSessionId(), e);
        }
    }
}