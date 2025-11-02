package server.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 간단한 JSON 파싱 유틸리티
 * 복잡한 JSON 라이브러리 대신 기본적인 key-value 파싱만 지원
 */
public final class JsonUtil {

    private JsonUtil() {}

    /**
     * 간단한 JSON 객체를 Map으로 파싱
     * 예: {"username":"test","password":"123"} -> Map
     */
    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> result = new HashMap<>();
        
        if (json == null || json.trim().isEmpty()) {
            return result;
        }

        String cleaned = json.trim();
        if (!cleaned.startsWith("{") || !cleaned.endsWith("}")) {
            return result;
        }

        // 중괄호 제거
        cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        
        if (cleaned.isEmpty()) {
            return result;
        }

        // 쉼표로 분리
        String[] pairs = cleaned.split(",");
        
        for (String pair : pairs) {
            pair = pair.trim();
            int colonIndex = pair.indexOf(':');
            
            if (colonIndex > 0 && colonIndex < pair.length() - 1) {
                String key = pair.substring(0, colonIndex).trim();
                String value = pair.substring(colonIndex + 1).trim();
                
                // 따옴표 제거
                key = removeQuotes(key);
                value = removeQuotes(value);
                
                if (!key.isEmpty()) {
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }

    /**
     * Map을 간단한 JSON 문자열로 변환
     */
    public static String toSimpleJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\"");
            json.append(":");
            json.append("\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }

    /**
     * 성공/실패 응답 JSON 생성
     */
    public static String createResponse(boolean success, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("success", String.valueOf(success));
        response.put("message", message != null ? message : "");
        return toSimpleJson(response);
    }

    public static String createListResponse(boolean success, String message, java.util.List<String> items) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":\"").append(success).append("\",");
        json.append("\"message\":\"").append(escapeJson(message != null ? message : "")).append("\",");
        json.append("\"posts\":[");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append("\"").append(escapeJson(items.get(i))).append("\"");
            }
        }
        json.append("]}");
        return json.toString();
    }

    /**
     * 따옴표 제거
     */
    private static String removeQuotes(String str) {
        if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    /**
     * JSON 문자열 이스케이프
     */
    public static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
