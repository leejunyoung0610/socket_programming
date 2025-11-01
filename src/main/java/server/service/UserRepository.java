package server.service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

import server.model.User;
import server.util.Logger;

/**
 * 파일 시스템 기반 사용자 저장소
 * users/ 디렉토리에 각 사용자별로 파일을 생성하여 정보를 저장
 */
public final class UserRepository {
    private static final Path USERS_DIR = Paths.get("users");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public UserRepository() {
        try {
            Files.createDirectories(USERS_DIR);
        } catch (IOException e) {
            Logger.error("Failed to create users directory", e);
        }
    }

    /**
     * 사용자 등록
     */
    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        Path userFile = USERS_DIR.resolve(username + ".txt");
        if (Files.exists(userFile)) {
            return false; // 이미 존재하는 사용자
        }

        try {
            String passwordHash = hashPassword(password);
            User user = new User(username, passwordHash);
            saveUser(user);
            Logger.info("User registered: " + username);
            return true;
        } catch (Exception e) {
            Logger.error("Failed to register user: " + username, e);
            return false;
        }
    }

    /**
     * 사용자 로그인 검증
     */
    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        Optional<User> userOpt = loadUser(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        String passwordHash = hashPassword(password);
        
        if (passwordHash.equals(user.getPasswordHash())) {
            // 로그인 시간 업데이트
            User updatedUser = user.withLastLogin(LocalDateTime.now());
            saveUser(updatedUser);
            Logger.info("User authenticated: " + username);
            return true;
        }
        
        return false;
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Path userFile = USERS_DIR.resolve(username + ".txt");
        return Files.exists(userFile);
    }

    /**
     * 사용자 정보 로드
     */
    public Optional<User> loadUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        Path userFile = USERS_DIR.resolve(username + ".txt");
        if (!Files.exists(userFile)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(userFile);
            String[] lines = content.split("\n");
            
            if (lines.length < 3) {
                return Optional.empty();
            }

            String storedUsername = lines[0].substring(9); // "username:" 제거
            String passwordHash = lines[1].substring(9);   // "password:" 제거
            String createdAtStr = lines[2].substring(10);  // "createdAt:" 제거
            
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr, DATETIME_FORMAT);
            LocalDateTime lastLoginAt = null;
            
            if (lines.length >= 4 && !lines[3].substring(12).equals("null")) {
                String lastLoginStr = lines[3].substring(12); // "lastLoginAt:" 제거
                lastLoginAt = LocalDateTime.parse(lastLoginStr, DATETIME_FORMAT);
            }

            return Optional.of(new User(storedUsername, passwordHash, createdAt, lastLoginAt));
        } catch (Exception e) {
            Logger.error("Failed to load user: " + username, e);
            return Optional.empty();
        }
    }

    /**
     * 사용자 정보 저장
     */
    private void saveUser(User user) {
        Path userFile = USERS_DIR.resolve(user.getUsername() + ".txt");
        
        StringBuilder content = new StringBuilder();
        content.append("username:").append(user.getUsername()).append("\n");
        content.append("password:").append(user.getPasswordHash()).append("\n");
        content.append("createdAt:").append(user.getCreatedAt().format(DATETIME_FORMAT)).append("\n");
        content.append("lastLoginAt:").append(
            user.getLastLoginAt() != null ? user.getLastLoginAt().format(DATETIME_FORMAT) : "null"
        ).append("\n");

        try {
            Files.writeString(userFile, content.toString());
        } catch (IOException e) {
            Logger.error("Failed to save user: " + user.getUsername(), e);
        }
    }

    /**
     * 비밀번호 해시화
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
