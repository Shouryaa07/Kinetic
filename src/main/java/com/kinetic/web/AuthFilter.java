package com.kinetic.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.kinetic.web.servlet.BaseServlet;
import java.io.IOException;

/** Sends visitors who are not signed in to the login page. Applied to the member-only URLs. */
@WebFilter(filterName = "AuthFilter",
        urlPatterns = {"/dashboard", "/activities", "/goals", "/vitals", "/progress", "/profile", "/export.csv", "/logout"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(BaseServlet.SESSION_USER) == null) {
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }
}
