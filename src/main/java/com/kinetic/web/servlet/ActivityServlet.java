package com.kinetic.web.servlet;

import com.kinetic.model.Activity;
import com.kinetic.model.ActivityType;
import com.kinetic.model.User;
import com.kinetic.web.view.ActivitiesView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/** Lists workouts (GET, optionally ?edit=id) and adds, updates or deletes them (POST action=add|update|delete). */
@WebServlet(name = "ActivityServlet", urlPatterns = "/activities")
public class ActivityServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = currentUser(req);
        ActivityType filter = ActivityType.parse(req.getParameter("type"));
        Activity editing = null;
        String edit = req.getParameter("edit");
        if (edit != null) {
            try {
                editing = app().activities.find(user.getId(), Long.parseLong(edit)).orElse(null);
            } catch (NumberFormatException ignored) {
                // a bad id simply opens the empty form
            }
        }
        html(resp, ActivitiesView.render(page(req), app().activities.list(user.getId(), filter, 200), filter, LocalDate.now(), editing));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = currentUser(req);
        if ("delete".equals(req.getParameter("action"))) {
            long id;
            try {
                id = Long.parseLong(req.getParameter("id"));
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            app().activities.delete(user.getId(), id);
            flash(req, "success", "Workout deleted.");
        } else if ("update".equals(req.getParameter("action"))) {
            long id;
            try {
                id = Long.parseLong(req.getParameter("id"));
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Activity a = app().activities.update(user.getId(), id, req.getParameter("type"), req.getParameter("date"),
                    req.getParameter("duration"), req.getParameter("distance"), req.getParameter("intensity"),
                    req.getParameter("notes"));
            flash(req, "success", a.type().label() + " updated: " + a.durationMinutes() + " min, about " + a.caloriesBurned() + " kcal.");
        } else {
            Activity a = app().activities.log(user.getId(), req.getParameter("type"), req.getParameter("date"),
                    req.getParameter("duration"), req.getParameter("distance"), req.getParameter("intensity"),
                    req.getParameter("notes"));
            flash(req, "success", a.type().label() + " saved: " + a.durationMinutes() + " min, about " + a.caloriesBurned() + " kcal.");
        }
        redirect(req, resp, "/activities");
    }
}
