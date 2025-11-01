package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;

/**
 * 라우터가 선택한 실제 처리 로직을 표현하는 인터페이스.
 */
public interface Handler {
    HttpResponse handle(HttpRequest request) throws IOException;
}
