package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

public final class HeadFilter implements Filter {
  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    HttpResponse res = chain.doFilter(req);
    if ("HEAD".equals(req.method())) {
      HttpResponse.Builder builder = HttpResponse.builder(res.statusCode(), res.reasonPhrase());
      res.headers().forEach(builder::header);
      builder.body(new byte[0]);
      return builder.build();
    }
    return res;
  }
}


/*
 * HeadFilter 는 HEAD 요청인지 확인한 뒤,
 * 체인 뒤쪽에서 만들어진 응답을 복사하여 본문을 제거한다.
 * 실제 응답 헤더(특히 Content-Length)는 그대로 유지된다.
 */
