package com.kinetic.web.servlet;

import com.kinetic.exception.KineticException;
import com.kinetic.model.User;
import com.kinetic.web.view.AuthView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Email and password sign-in. */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (currentUser(req) != null) {
            redirect(req, resp, "/dashboard");
            return;
        }
        html(resp, AuthView.login(page(req), app().demoEnabled, ""));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = app().auth.login(req.getParameter("email"), req.getParameter("password"));
        signIn(req, user);
        redirect(req, resp, "/dashboard");
    }

    /** Re-shows the form with the error and keeps the email the visitor typed. */
    @Override
    protected void onBusinessError(HttpServletRequest req, HttpServletResponse resp, KineticException e)
            throws java.io.IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String email = req.getParameter("email") == null ? "" : req.getParameter("email");
        html(resp, AuthView.login(page(req, "error", e.getMessage()), app().demoEnabled, email));
    }
}
