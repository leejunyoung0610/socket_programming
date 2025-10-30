package server.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import server.util.Logger;

/**
 * 게시글 관리 서비스
 * posts/ 디렉토리에 텍스트 파일로 게시글을 저장
 */
public final class PostService {
    private static final Path POSTS_DIR = Paths.get("www", "posts");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PostService() {
        try {
            Files.createDirectories(POSTS_DIR);
        } catch (IOException e) {
            Logger.error("Failed to create posts directory", e);
        }
    }

    /**
     * 새 게시글 작성
     */
    public boolean createPost(String title, String content, String author) {
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return false;
        }

        try {
            String filename = generateFilename(title);
            Path postFile = POSTS_DIR.resolve(filename);
            
            StringBuilder postContent = new StringBuilder();
            postContent.append("제목: ").append(title).append("\n");
            postContent.append("작성자: ").append(author != null ? author : "익명").append("\n");
            postContent.append("작성일: ").append(LocalDateTime.now().format(DATETIME_FORMAT)).append("\n");
            postContent.append("---\n");
            postContent.append(content);

            Files.writeString(postFile, postContent.toString());
            Logger.info("Post created: " + filename);
            return true;
        } catch (Exception e) {
            Logger.error("Failed to create post: " + title, e);
            return false;
        }
    }

    /**
     * 게시글 목록 조회
     */
    public List<String> listPosts() {
        List<String> posts = new ArrayList<>();
        try {
            Files.list(POSTS_DIR)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(path -> posts.add(path.getFileName().toString()));
        } catch (Exception e) {
            Logger.error("Failed to list posts", e);
        }
        return posts;
    }

    /**
     * 파일명 생성 (제목을 안전한 파일명으로 변환)
     */
    private String generateFilename(String title) {
        String safe = title.replaceAll("[^a-zA-Z0-9가-힣\\s]", "")
                          .replaceAll("\\s+", "_")
                          .toLowerCase();
        return safe + "_" + System.currentTimeMillis() + ".txt";
    }
}