package server.route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.service.PostService;
import server.util.JsonUtil;

/**
 * 게시글 목록을 JSON으로 반환하는 핸들러
 */
public final class PostListHandler implements Handler {
    private final PostService postService;

    public PostListHandler(PostService postService) {
        this.postService = postService;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        if (!"GET".equals(request.method())) {
            return methodNotAllowed();
        }

        try {
            List<String> posts = postService.listPosts();
            
            // JSON 배열 형태로 변환
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < posts.size(); i++) {
                String filename = posts.get(i);
                String title = extractTitleFromFilename(filename);
                
                json.append("{")
                    .append("\"id\":").append(i + 100).append(",")  // ID는 100부터 시작
                    .append("\"title\":\"").append(escapeJson(title)).append("\",")
                    .append("\"path\":\"/posts/").append(escapeJson(filename)).append("\",")
                    .append("\"type\":\"text\"")
                    .append("}");
                
                if (i < posts.size() - 1) {
                    json.append(",");
                }
            }
            
            json.append("]");

            return HttpResponse.builder(200, "OK")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(json.toString().getBytes(StandardCharsets.UTF_8))
                    .build();
                    
        } catch (Exception e) {
            return internalServerError("게시글 목록 조회 중 오류가 발생했습니다.");
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
}
