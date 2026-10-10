package com.kinetic.web.servlet;

import com.kinetic.web.view.LandingView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Public home page at the context root. Signed-in members go straight to the dashboard. */
@WebServlet(name = "LandingServlet", urlPatterns = {""})
public class LandingServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (currentUser(req) != null) {
            redirect(req, resp, "/dashboard");
            return;
        }
        html(resp, LandingView.render(page(req)));
    }
}
