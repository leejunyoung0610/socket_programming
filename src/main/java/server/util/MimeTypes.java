package server.util;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 파일 확장자를 간단한 MIME 타입 문자열로 매핑해 주는 유틸리티.
 */
public final class MimeTypes {
    private static final Map<String, String> TYPES = new HashMap<>();

    static {
        TYPES.put("html", "text/html; charset=UTF-8");
        TYPES.put("htm", "text/html; charset=UTF-8");
        TYPES.put("css", "text/css; charset=UTF-8");
        TYPES.put("js", "application/javascript; charset=UTF-8");
        TYPES.put("json", "application/json; charset=UTF-8");
        TYPES.put("png", "image/png");
        TYPES.put("jpg", "image/jpeg");
        TYPES.put("jpeg", "image/jpeg");
        TYPES.put("gif", "image/gif");
        TYPES.put("txt", "text/plain; charset=UTF-8");
    }

    private MimeTypes() {}

    public static String probe(Path path) {
        String filename = path.getFileName().toString();
        int idx = filename.lastIndexOf('.');
        if (idx != -1 && idx < filename.length() - 1) {
            String ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT);
            String type = TYPES.get(ext);
            if (type != null) {
                return type;
            }
        }
        return "application/octet-stream";
    }
}
