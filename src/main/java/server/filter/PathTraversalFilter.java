package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;
import java.nio.file.Path;

import static server.http.ErrorResponses.*;

public final class PathTraversalFilter implements Filter {
  private final Path webRoot;
  private final String home;

  public PathTraversalFilter(Path webRoot, String home) {
    this.webRoot = webRoot.normalize().toAbsolutePath();
    this.home = home;
  }

  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    String path = req.path();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    Path resolved = webRoot.resolve(path.isEmpty() ? "." : path).normalize();
    if (!resolved.startsWith(webRoot)) {
      return forbiddenAlert(req, "잘못된 요청입니다.", home);
    }
    return chain.doFilter(req);
  }
}


/*
 * 파싱된 요청의 경로가 정확한 웹 루트를 따르는지에 대한 여부를 판단하는 필터라
 */
