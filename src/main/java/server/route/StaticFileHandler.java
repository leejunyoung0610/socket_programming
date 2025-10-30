package server.route;

import server.config.ServerConfig;
import server.http.HttpRequest;
import server.http.HttpResponse;
import server.util.MimeTypes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 요청 경로를 기반으로 정적 파일을 찾아 반환하는 기본 핸들러.
 */
public final class StaticFileHandler implements Handler {
    private final Path root;

    public StaticFileHandler(Path root) {
        // 생성자이다. 루트 경로를 절대 경로로 정규화하여 저장한다.
        this.root = root.normalize().toAbsolutePath();
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        String target = request.target();
        // 쿼리 스트링을 제외한 경로만 사용한다.
        String pathPart = target.split("\\?", 2)[0];
        if (pathPart.isEmpty()) {
            pathPart = "/";
        }
        Path resolved = resolvePath(pathPart);
        if (resolved == null) {
            return forbidden();
        }
        if (Files.isDirectory(resolved)) {
            resolved = resolved.resolve("index.html");
        }
        if (!Files.exists(resolved) || !Files.isReadable(resolved)) {
            return notFound();
        }
        byte[] body = Files.readAllBytes(resolved);
        String mime = MimeTypes.probe(resolved);
        return HttpResponse.builder(200, "OK")
                .header("Content-Type", mime)
                .body(body)
                .build();
    }

    private Path resolvePath(String pathPart) {
        String cleaned = stripLeadingSlash(pathPart);
        if (cleaned.contains("..")) {
            // 상대경로 탈출 시도는 즉시 차단한다.
            return null;
        }
        Path candidate = root.resolve(cleaned).normalize();
        if (!candidate.startsWith(root)) {
            return null;
        }
        return candidate;
    }

    private String stripLeadingSlash(String value) {
        if (value.startsWith("/")) {
            return value.substring(1);
        }
        return value;
    }

    private HttpResponse forbidden() {
        return HttpResponse.builder(403, "Forbidden")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Forbidden".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse notFound() {
        return HttpResponse.builder(404, "Not Found")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Not Found".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
