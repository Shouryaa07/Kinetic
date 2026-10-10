package com.kinetic.web.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Liveness probe used by deployments and the README smoke test. */
@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public class HealthServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"ok\",\"online\":" + app().online.count() + "}");
    }
}
