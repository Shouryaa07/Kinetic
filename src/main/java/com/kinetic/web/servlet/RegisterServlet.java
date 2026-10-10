package com.kinetic.web.servlet;

import com.kinetic.exception.KineticException;
import com.kinetic.model.User;
import com.kinetic.web.view.AuthView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Account creation with email and password. */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (currentUser(req) != null) {
            redirect(req, resp, "/dashboard");
            return;
        }
        html(resp, AuthView.register(page(req), "", ""));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = app().auth.register(req.getParameter("name"), req.getParameter("email"), req.getParameter("password"));
        signIn(req, user);
        flash(req, "success", "Welcome to Kinetic, " + user.firstName() + "! Log your first workout to get started.");
        redirect(req, resp, "/dashboard");
    }

    @Override
    protected void onBusinessError(HttpServletRequest req, HttpServletResponse resp, KineticException e)
            throws java.io.IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String name = req.getParameter("name") == null ? "" : req.getParameter("name");
        String email = req.getParameter("email") == null ? "" : req.getParameter("email");
        html(resp, AuthView.register(page(req, "error", e.getMessage()), name, email));
    }
}
