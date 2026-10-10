package com.kinetic.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.kinetic.util.Config;
import java.io.IOException;

/** Adds security headers to every response and rejects POST requests without a valid CSRF token. */
@WebFilter(filterName = "SecurityFilter", urlPatterns = "/*")
public class SecurityFilter implements Filter {
    private static final String CSP = "default-src 'self'; "
            + "script-src 'self' https://accounts.google.com/gsi/client; "
            + "style-src 'self' 'unsafe-inline' https://accounts.google.com/gsi/style; "
            + "img-src 'self' data: https:; "
            + "connect-src 'self' https://accounts.google.com/gsi/; "
            + "frame-src https://accounts.google.com/gsi/; "
            + "form-action 'self'; frame-ancestors 'none'; base-uri 'self'";

    private final boolean https = Config.getBool("cookie.secure", false);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Content-Security-Policy", CSP);
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        res.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
        if (https) {
            res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if ("POST".equals(req.getMethod()) && !Csrf.valid(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Your session expired. Reload the page and try again.");
            return;
        }
        chain.doFilter(request, response);
    }
}
