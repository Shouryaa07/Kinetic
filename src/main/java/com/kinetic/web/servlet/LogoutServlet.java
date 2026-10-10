package com.kinetic.web.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Ends the session. POST only, so a stray link cannot sign anyone out. */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends BaseServlet {
    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirect(req, resp, "/");
    }
}
