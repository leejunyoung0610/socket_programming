package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import server.config.ServerConfig;
import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.PostService;
import server.util.JsonUtil;

/**
 * 텍스트 게시글과 이미지를 모두 포함한 통합 콘텐츠 목록을 JSON으로 반환하는 핸들러
 */
public final class ContentListHandler implements Handler {
    private final PostService postService;
    private final Path imagesDir;

    public ContentListHandler(PostService postService) {
        this.postService = postService;
        this.imagesDir = ServerConfig.WEB_ROOT.resolve("images");
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        if (!"GET".equals(request.method())) {
            return methodNotAllowed();
        }

        try {
            List<ContentItem> allContent = new ArrayList<>();
            int idCounter = 100; // ID는 100부터 시작

            // 1. 텍스트 게시글 추가
            List<String> posts = postService.listPosts();
            for (String filename : posts) {
                String title = extractTitleFromFilename(filename);
                allContent.add(new ContentItem(
                    idCounter++, 
                    title, 
                    "/posts/" + filename, 
                    "text"
                ));
            }

            // 2. 이미지 파일 추가
            if (Files.exists(imagesDir)) {
                try (Stream<Path> imageStream = Files.list(imagesDir)) {
                    List<Path> imageFiles = imageStream
                        .filter(Files::isRegularFile)
                        .filter(path -> isImageFile(path.getFileName().toString()))
                        .toList();
                    
                    for (Path path : imageFiles) {
                        String filename = path.getFileName().toString();
                        String title = extractImageTitle(filename);
                        allContent.add(new ContentItem(
                            idCounter++, 
                            title, 
                            "/images/" + filename, 
                            "image"
                        ));
                    }
                }
            }

            // JSON 배열 생성
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < allContent.size(); i++) {
                ContentItem item = allContent.get(i);
                json.append("{")
                    .append("\"id\":").append(item.id).append(",")
                    .append("\"title\":\"").append(escapeJson(item.title)).append("\",")
                    .append("\"path\":\"").append(escapeJson(item.path)).append("\",")
                    .append("\"type\":\"").append(item.type).append("\"")
                    .append("}");
                
                if (i < allContent.size() - 1) {
                    json.append(",");
                }
            }
            
            json.append("]");

            return HttpResponse.builder(200, "OK")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(json.toString().getBytes(StandardCharsets.UTF_8))
                    .build();
                    
        } catch (Exception e) {
            return internalServerError("콘텐츠 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일명에서 제목 추출 (타임스탬프 제거)
     */
    private String extractTitleFromFilename(String filename) {
        if (filename.endsWith(".txt")) {
            String nameWithoutExt = filename.substring(0, filename.length() - 4);
            // 마지막 underscore와 숫자 제거 (타임스탬프 부분)
            int lastUnderscore = nameWithoutExt.lastIndexOf('_');
            if (lastUnderscore > 0) {
                String beforeTimestamp = nameWithoutExt.substring(0, lastUnderscore);
                // underscore를 공백으로 변경
                return beforeTimestamp.replace('_', ' ');
            }
            return nameWithoutExt.replace('_', ' ');
        }
        return filename;
    }

    /**
     * 이미지 파일명에서 제목 추출
     */
    private String extractImageTitle(String filename) {
        // 확장자 제거
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            String nameWithoutExt = filename.substring(0, dotIndex);
            // underscore를 공백으로 변경하고 첫 글자를 대문자로
            String title = nameWithoutExt.replace('_', ' ').replace('-', ' ');
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        return filename;
    }

    /**
     * 이미지 파일인지 확인
     */
    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
               lower.endsWith(".png") || lower.endsWith(".gif") || 
               lower.endsWith(".webp") || lower.endsWith(".svg");
    }

    /**
     * JSON 문자열 이스케이프 처리
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        
        StringBuilder escaped = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c <= '\u001F') {
                        escaped.append(String.format("\\u%04X", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private HttpResponse methodNotAllowed() {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", "GET")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse internalServerError(String message) {
        return HttpResponse.builder(500, "Internal Server Error")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JsonUtil.createResponse(false, message).getBytes(StandardCharsets.UTF_8))
                .build();
    }

    /**
     * 콘텐츠 아이템을 나타내는 내부 클래스
     */
    private static class ContentItem {
        final int id;
        final String title;
        final String path;
        final String type;

        ContentItem(int id, String title, String path, String type) {
            this.id = id;
            this.title = title;
            this.path = path;
            this.type = type;
        }
    }
}
