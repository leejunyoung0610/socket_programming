package server.http;

/**
 * HTTP 요청 파싱 중 형식 오류가 발생했을 때 사용되는 예외.
 */
public class HttpParseException extends Exception {
    public HttpParseException(String message) {
        super(message);
    }
}
