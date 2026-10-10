package com.kinetic.web.servlet;

import com.kinetic.util.Html;
import com.kinetic.web.PageContext;
import com.kinetic.web.view.Icons;
import com.kinetic.web.view.Layout;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Friendly error page, wired up through web.xml for 403, 404 and 500. */
@WebServlet(name = "ErrorServlet", urlPatterns = "/error")
public class ErrorServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Object code = req.getAttribute("jakarta.servlet.error.status_code");
        int status = code instanceof Integer i ? i : 500;
        String message = req.getAttribute("jakarta.servlet.error.message") instanceof String s && status == 403
                ? s : switch (status) {
            case 404 -> "We could not find that page.";
            case 403 -> "You do not have access to that.";
            default -> "Something went wrong on our side. Please try again.";
        };
        PageContext p = page(req, null, null);
        String body = Layout.publicNav(p) + "<section class=\"error-page\">" + Icons.logo(48) + "<h1>" + status + "</h1><p>"
                + Html.esc(message) + "</p><a class=\"btn btn-primary\" href=\"" + p.url("/") + "\">Back to Kinetic</a></section>";
        resp.setStatus(status);
        html(resp, Layout.publicPage(p, "Error " + status, "pub", body));
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        get(req, resp);
    }
}
