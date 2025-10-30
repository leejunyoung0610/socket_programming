package server.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 서버에서 생성한 HTTP 응답의 불변 표현.
 * 상태 코드/이유구문/헤더/본문을 한 번 세팅하면 외부에서 수정할 수 없다.
 */
public final class HttpResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(int statusCode,
                         String reasonPhrase,
                         Map<String, String> headers,
                         byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? new byte[0] : body.clone();
    }

    public int statusCode() {
        return statusCode;
    }

    public String reasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        return body.clone();
    }

    public static Builder builder(int statusCode, String reasonPhrase) {
        return new Builder(statusCode, reasonPhrase);
    }

    public static final class Builder {
        private final int statusCode;
        private final String reasonPhrase;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private byte[] body = new byte[0];

        private Builder(int statusCode, String reasonPhrase) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
        }

        /** 응답 헤더를 추가한다. 같은 이름이 들어오면 덮어쓴다. */
        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /** 본문 데이터를 설정한다. null 이면 빈 배열로 처리한다. */
        public Builder body(byte[] body) {
            this.body = body == null ? new byte[0] : body.clone();
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(statusCode, reasonPhrase, new LinkedHashMap<>(headers), body);
        }
    }
}
