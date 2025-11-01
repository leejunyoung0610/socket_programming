package server.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import server.http.HttpRequest;
import server.http.HttpResponse;

/**
 * HTTP 쿠키 처리 유틸리티
 */
public final class CookieUtil {

    private CookieUtil() {}

    /**
     * 요청에서 쿠키 파싱
     */
    public static Map<String, String> parseCookies(HttpRequest request) {
        Map<String, String> cookies = new HashMap<>();
        String cookieHeader = request.header("cookie");
        
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return cookies;
        }

        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String name = URLDecoder.decode(keyValue[0].trim(), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1].trim(), StandardCharsets.UTF_8);
                    cookies.put(name, value);
                } catch (Exception e) {
                    // 디코딩 실패 시 무시
                }
            }
        }
        
        return cookies;
    }

    /**
     * 쿠키 값 가져오기
     */
    public static String getCookie(HttpRequest request, String name) {
        Map<String, String> cookies = parseCookies(request);
        return cookies.get(name);
    }

    /**
     * 응답에 쿠키 설정 (세션 쿠키)
     */
    public static void setCookie(HttpResponse.Builder responseBuilder, String name, String value) {
        setCookie(responseBuilder, name, value, -1, "/", false, true);
    }

    /**
     * 응답에 쿠키 설정 (상세 옵션)
     */
    public static void setCookie(HttpResponse.Builder responseBuilder, String name, String value, 
                                int maxAgeSeconds, String path, boolean secure, boolean httpOnly) {
        try {
            StringBuilder cookie = new StringBuilder();
            cookie.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
            cookie.append("=");
            cookie.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            
            if (maxAgeSeconds >= 0) {
                cookie.append("; Max-Age=").append(maxAgeSeconds);
            }
            
            if (path != null && !path.isEmpty()) {
                cookie.append("; Path=").append(path);
            }
            
            if (secure) {
                cookie.append("; Secure");
            }
            
            if (httpOnly) {
                cookie.append("; HttpOnly");
            }
            
            responseBuilder.header("Set-Cookie", cookie.toString());
        } catch (Exception e) {
            Logger.error("Failed to set cookie: " + name, e);
        }
    }

    /**
     * 쿠키 삭제 (만료시킴)
     */
    public static void deleteCookie(HttpResponse.Builder responseBuilder, String name, String path) {
        setCookie(responseBuilder, name, "", 0, path, false, true);
    }

    /**
     * 세션 쿠키 설정
     */
    public static void setSessionCookie(HttpResponse.Builder responseBuilder, String sessionId) {
        setCookie(responseBuilder, "JSESSIONID", sessionId, -1, "/", false, true);
    }

    /**
     * 세션 쿠키 삭제
     */
    public static void deleteSessionCookie(HttpResponse.Builder responseBuilder) {
        deleteCookie(responseBuilder, "JSESSIONID", "/");
    }

    /**
     * 요청에서 세션 ID 가져오기
     */
    public static String getSessionId(HttpRequest request) {
        return getCookie(request, "JSESSIONID");
    }
}