package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

public final class LoggingFilter implements Filter {
  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    long t0 = System.nanoTime();
    HttpResponse res = chain.doFilter(req);
    long ms = (System.nanoTime() - t0) / 1_000_000;
    System.out.printf("%s %s -> %d (%d ms)%n", req.method(), req.path(), res.statusCode(), ms);
    return res;
  }
}


/*
 * LoggingFilter 는 성공/실패와 상관없이 요청 로그만 남기는 역할이에요.
 * 
 */
