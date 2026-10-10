package com.kinetic.web.servlet;

import com.kinetic.model.GoalMetric;
import com.kinetic.model.User;
import com.kinetic.web.view.GoalsView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.EnumMap;
import java.util.Map;

/** View and save weekly goals. */
@WebServlet(name = "GoalServlet", urlPatterns = "/goals")
public class GoalServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = currentUser(req);
        html(resp, GoalsView.render(page(req), app().stats.dashboard(user.getId()).goals()));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Map<GoalMetric, String> raw = new EnumMap<>(GoalMetric.class);
        for (GoalMetric m : GoalMetric.values()) {
            raw.put(m, req.getParameter(m.name()));
        }
        app().goals.saveAll(currentUser(req).getId(), raw);
        flash(req, "success", "Goals saved.");
        redirect(req, resp, "/goals");
    }
}
