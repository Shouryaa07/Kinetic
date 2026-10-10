package com.kinetic.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Per-session anti-CSRF token. Every POST must echo it in the {@code _csrf} field. */
public final class Csrf {
    private static final String ATTR = "kinetic.csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Csrf() {
    }

    public static String token(HttpSession session) {
        Object existing = session.getAttribute(ATTR);
        if (existing != null) {
            return existing.toString();
        }
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(ATTR, token);
        return token;
    }

    public static boolean valid(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String sent = req.getParameter("_csrf");
        if (session == null || sent == null || session.getAttribute(ATTR) == null) {
            return false;
        }
        return MessageDigest.isEqual(sent.getBytes(StandardCharsets.UTF_8),
                session.getAttribute(ATTR).toString().getBytes(StandardCharsets.UTF_8));
    }
}
