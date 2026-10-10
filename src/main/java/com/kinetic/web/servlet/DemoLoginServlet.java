package com.kinetic.web.servlet;

import com.kinetic.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

/** One-click access to the seeded demo account, so reviewers can explore without registering. */
@WebServlet(name = "DemoLoginServlet", urlPatterns = "/auth/demo")
public class DemoLoginServlet extends BaseServlet {
    /** Pages the demo link may open directly. Anything else falls back to the dashboard. */
    private static final Set<String> LANDING = Set.of("/dashboard", "/activities", "/goals", "/vitals", "/progress", "/profile");

    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Optional<User> demo = app().demoEnabled ? app().auth.demoUser() : Optional.empty();
        if (demo.isEmpty()) {
            flash(req, "error", "The demo account is not available.");
            redirect(req, resp, "/login");
            return;
        }
        signIn(req, demo.get());
        String next = req.getParameter("next");
        redirect(req, resp, next != null && LANDING.contains(next) ? next : "/dashboard");
    }
}
