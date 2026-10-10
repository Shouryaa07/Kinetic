package com.kinetic.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses a flat JSON object ({"key": "value", "n": 12, "ok": true}) into a string map.
 * It exists so the project needs no JSON library for Google's token response. Nested values
 * are not supported and are rejected.
 */
public final class JsonLite {
    private final String src;
    private int pos;

    private JsonLite(String src) {
        this.src = src;
    }

    public static Map<String, String> parseObject(String json) {
        return new JsonLite(json == null ? "" : json).object();
    }

    private Map<String, String> object() {
        Map<String, String> out = new LinkedHashMap<>();
        skipWs();
        expect('{');
        skipWs();
        if (peek() == '}') {
            return out;
        }
        while (true) {
            skipWs();
            String key = string();
            skipWs();
            expect(':');
            skipWs();
            out.put(key, value());
            skipWs();
            char c = next();
            if (c == '}') {
                return out;
            }
            if (c != ',') {
                throw new IllegalArgumentException("Expected ',' or '}' at " + pos);
            }
        }
    }

    private String value() {
        char c = peek();
        if (c == '"') {
            return string();
        }
        if (c == '{' || c == '[') {
            throw new IllegalArgumentException("Nested JSON is not supported");
        }
        int start = pos;
        while (pos < src.length() && ",}".indexOf(src.charAt(pos)) < 0) {
            pos++;
        }
        return src.substring(start, pos).trim();
    }

    private String string() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = next();
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                    pos += 4;
                }
                default -> sb.append(e);
            }
        }
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        if (pos >= src.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON");
        }
        return src.charAt(pos);
    }

    private char next() {
        char c = peek();
        pos++;
        return c;
    }

    private void expect(char c) {
        if (next() != c) {
            throw new IllegalArgumentException("Expected '" + c + "' at " + (pos - 1));
        }
    }
}
