package server.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 파싱된 HTTP 요청 데이터를 보관하는 불변 객체.
 * 메소드/경로/버전/헤더/본문 정보를 한 번 받아오면 외부에서 수정할 수 없다.
 */
public final class HttpRequest {
    private final String method;
    private final String target;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpRequest(String method,
                       String target,
                       String version,
                       Map<String, String> headers,
                       byte[] body) {
        this.method = method;
        this.target = target;
        this.version = version;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? new byte[0] : body.clone();
    }

    public String method() {
        return method;
    }

    public String target() {
        return target;
    }

    /**
     * 질의 문자열을 제외한 경로를 반환한다.
     */
    public String path() {
        int idx = target.indexOf('?');
        if (idx == -1) {
            return target.isEmpty() ? "/" : target;
        }
        String path = target.substring(0, idx);
        return path.isEmpty() ? "/" : path;
    }

    public String version() {
        return version;
    }

    public Map<String, String> headers() {
        return headers;
    }

    /**
     * 헤더 이름을 소문자로 통일해 저장했으므로, 조회 시에도 소문자 키로 접근한다.
     */
    public String header(String name) {
        return headers.get(name.toLowerCase());
    }

    public byte[] body() {
        return body.clone();
    }
}
