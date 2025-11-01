
package server.route;

import java.io.IOException;

import server.http.HttpRequest;
import server.http.HttpResponse;

/**
 * ExtendedRouter를 기존 Router 인터페이스에 맞추는 어댑터
 * 기존 코드 (NetAcceptor)를 수정하지 않고 새로운 라우터를 사용하기 위함
 */
public final class RouterAdapter {
    private final ExtendedRouter extendedRouter;

    public RouterAdapter(ExtendedRouter extendedRouter) {
        this.extendedRouter = extendedRouter;
    }

    /**
     * 기존 Router.route() 메소드와 같은 시그니처
     */
    public HttpResponse route(HttpRequest request) throws IOException {
        return extendedRouter.route(request);
    }
}
