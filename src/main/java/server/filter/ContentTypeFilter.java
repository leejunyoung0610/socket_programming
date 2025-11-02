package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static server.http.ErrorResponses.unsupportedMediaTypeAlert;

/**
 * 본문이 있는 요청(POST/PUT/PATCH)에 대해 Content-Type을 검증한다.
 * - Content-Type 누락 또는 비허용 타입이면 415 Unsupported Media Type을 반환한다(브라우저: alert + 홈 이동).
 * - 파라미터(; charset=..., ; boundary=...)는 무시하고 type/subtype만 비교한다.
 */
public final class ContentTypeFilter implements Filter {

  private final String home;
  private final Set<String> allowedTypes; // 소문자, 예: application/json, application/x-www-form-urlencoded, multipart/form-data

  /**
   * 기본 허용: JSON, 폼-URL-encoded, 멀티파트
   */
  public static ContentTypeFilter withDefaults(String home) {
    return new ContentTypeFilter(
        home,
        new HashSet<>(Arrays.asList(
            "application/json",
            "application/x-www-form-urlencoded",
            "multipart/form-data"
        ))
    );
  }

  public ContentTypeFilter(String home, Set<String> allowedTypes) {
    this.home = home;
    this.allowedTypes = (allowedTypes == null) ? Collections.emptySet() : normalize(allowedTypes);
  }

  @Override
  public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    final String method = req.method();

    // 바디를 가질 수 있는 메서드만 검사 (필요 시 PATCH/DELETE 등 확장 가능)
    if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
      return chain.doFilter(req);
    }

    // 바디가 없으면 Content-Type 검증 생략
    final String te = Optional.ofNullable(req.header("transfer-encoding")).orElse("");
    final long len = Optional.ofNullable(req.header("Content-Length")).map(Long::parseLong).orElse(0L);
    final boolean hasBody = len > 0 || te.toLowerCase(Locale.ROOT).contains("chunked");
    if (!hasBody) {
      return chain.doFilter(req);
    }

    // Content-Type 검증
    final String raw = Optional.ofNullable(req.header("content-type")).orElse("").trim();
    if (raw.isEmpty()) {
      return unsupportedMediaTypeAlert(req, "요청 본문에 Content-Type 헤더가 없습니다.", home);
    }

    // 파라미터 제거 후 type/subtype만 비교 (e.g., "application/json; charset=utf-8" → "application/json")
    String mediaType = raw.toLowerCase(Locale.ROOT);
    int semi = mediaType.indexOf(';');
    if (semi >= 0) {
      mediaType = mediaType.substring(0, semi).trim();
    }

    if (!allowedTypes.contains(mediaType)) {
      return unsupportedMediaTypeAlert(req, "지원하지 않는 미디어 타입입니다: " + mediaType, home);
    }

    return chain.doFilter(req);
  }

  private static Set<String> normalize(Set<String> types) {
    Set<String> out = new HashSet<>();
    for (String t : types) {
      if (t == null) continue;
      out.add(t.toLowerCase(Locale.ROOT).trim());
    }
    return out;
  }
}
