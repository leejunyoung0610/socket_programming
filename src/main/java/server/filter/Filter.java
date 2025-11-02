package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

public interface Filter {
  HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception;
}
