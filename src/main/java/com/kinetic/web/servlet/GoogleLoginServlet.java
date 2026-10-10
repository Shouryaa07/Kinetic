package com.kinetic.web.servlet;

import com.kinetic.exception.KineticException;
import com.kinetic.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Receives the Google ID token (the "credential") posted by google.js after the member picks an
 * account in Google's popup. The token is verified server-side; the browser is never trusted.
 */
@WebServlet(name = "GoogleLoginServlet", urlPatterns = "/auth/google")
public class GoogleLoginServlet extends BaseServlet {
    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = app().auth.loginWithGoogle(req.getParameter("credential"));
        signIn(req, user);
        flash(req, "success", "Signed in with Google as " + user.getEmail());
        redirect(req, resp, "/dashboard");
    }

    @Override
    protected void onBusinessError(HttpServletRequest req, HttpServletResponse resp, KineticException e)
            throws java.io.IOException {
        flash(req, "error", e.getMessage());
        redirect(req, resp, "/login");
    }
}
