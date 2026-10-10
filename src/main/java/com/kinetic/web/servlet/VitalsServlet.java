package com.kinetic.web.servlet;

import com.kinetic.model.User;
import com.kinetic.web.view.VitalsView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Daily health page (GET) and its three forms (POST action=daily|water|weight). */
@WebServlet(name = "VitalsServlet", urlPatterns = "/vitals")
public class VitalsServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = currentUser(req);
        html(resp, VitalsView.render(page(req), app().stats.dashboard(user.getId())));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long id = currentUser(req).getId();
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");
        switch (action) {
            case "water" -> {
                app().health.addWater(id, req.getParameter("ml"));
                flash(req, "success", "Water added.");
            }
            case "weight" -> {
                app().health.recordWeight(id, req.getParameter("date"), req.getParameter("weight"));
                flash(req, "success", "Weight saved.");
            }
            case "daily" -> {
                app().health.saveDaily(id, req.getParameter("date"), req.getParameter("steps"),
                        req.getParameter("water"), req.getParameter("sleep"));
                flash(req, "success", "Day saved.");
            }
            default -> {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        redirect(req, resp, "/vitals");
    }
}
