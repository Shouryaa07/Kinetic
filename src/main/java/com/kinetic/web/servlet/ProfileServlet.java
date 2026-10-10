package com.kinetic.web.servlet;

import com.kinetic.web.view.ProfileView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** View and edit name, weight and height. */
@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        html(resp, ProfileView.render(page(req)));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        com.kinetic.model.User user = currentUser(req);
        double before = user.getWeightKg();
        app().auth.updateProfile(user, req.getParameter("name"), req.getParameter("weight"), req.getParameter("height"));
        if (Math.abs(user.getWeightKg() - before) > 0.01) {
            // a changed weight is also a weigh-in, so the Health chart stays consistent
            app().health.recordWeight(user.getId(), java.time.LocalDate.now(), user.getWeightKg());
        }
        flash(req, "success", "Profile updated.");
        redirect(req, resp, "/profile");
    }
}
