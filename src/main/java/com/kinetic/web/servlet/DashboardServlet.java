package com.kinetic.web.servlet;

import com.kinetic.model.User;
import com.kinetic.web.view.DashboardView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** The member's home screen. */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = currentUser(req);
        html(resp, DashboardView.render(page(req), app().stats.dashboard(user.getId())));
    }
}
