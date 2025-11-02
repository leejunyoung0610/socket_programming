package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.route.Router;

import java.util.List;

public final class FilterChain {
  private final List<Filter> filters;
  private final Router router;
  private int index = 0;

  public FilterChain(List<Filter> filters, Router router) {
    this.filters = filters;
    this.router = router;
  }

  public HttpResponse doFilter(HttpRequest req) throws Exception {
    if (index < filters.size()) {
      return filters.get(index++).doFilter(req, this);
    }
    return router.route(req); // 마지막: 라우팅
  }
}
