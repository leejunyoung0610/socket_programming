
package server.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 세션 정보를 담는 모델 클래스
 */
public final class Session {
    private final String sessionId;
    private final String username;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public Session(String username, long durationMinutes) {
        this.sessionId = UUID.randomUUID().toString();
        this.username = username;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(durationMinutes);
    }

    public Session(String sessionId, String username, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.username = username;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired();
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", expired=" + isExpired() +
                '}';
    }
}
