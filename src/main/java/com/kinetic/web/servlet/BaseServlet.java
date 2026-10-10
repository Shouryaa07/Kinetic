package com.kinetic.web.servlet;

import com.kinetic.AppContext;
import com.kinetic.exception.KineticException;
import com.kinetic.model.User;
import com.kinetic.web.Csrf;
import com.kinetic.web.PageContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Template for every page servlet. Subclasses override {@link #get} and/or {@link #post}; this
 * class supplies the shared plumbing and one central error policy: business errors
 * ({@link KineticException}) become a flash message, anything else becomes a 500 page.
 */
public abstract class BaseServlet extends HttpServlet {
    public static final String SESSION_USER = "kinetic.uid";
    private static final String FLASH_TYPE = "kinetic.flash.type";
    private static final String FLASH_MSG = "kinetic.flash.msg";
    private static final String CACHED_USER = "kinetic.user";
    private static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());

    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** Default reaction to a business error: show it on the same page. Subclasses may override. */
    protected void onBusinessError(HttpServletRequest req, HttpServletResponse resp, KineticException e)
            throws IOException {
        flash(req, "error", e.getMessage());
        redirect(req, resp, req.getServletPath());
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp, false);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        dispatch(req, resp, true);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp, boolean isPost) throws IOException {
        try {
            if (isPost) {
                post(req, resp);
            } else {
                get(req, resp);
            }
        } catch (KineticException e) {
            onBusinessError(req, resp, e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unhandled error for " + req.getRequestURI(), e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    protected AppContext app() {
        return (AppContext) getServletContext().getAttribute(AppContext.KEY);
    }

    /** The signed-in member, or null. Looked up once per request. */
    protected User currentUser(HttpServletRequest req) {
        Object cached = req.getAttribute(CACHED_USER);
        if (cached instanceof User u) {
            return u;
        }
        HttpSession session = req.getSession(false);
        if (session == null || !(session.getAttribute(SESSION_USER) instanceof Long id)) {
            return null;
        }
        User user = app().auth.find(id).orElse(null);
        if (user == null) {
            session.invalidate();
        } else {
            req.setAttribute(CACHED_USER, user);
        }
        return user;
    }

    /** Builds the view context and consumes any pending flash message. */
    protected PageContext page(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        String type = (String) session.getAttribute(FLASH_TYPE);
        String msg = (String) session.getAttribute(FLASH_MSG);
        session.removeAttribute(FLASH_TYPE);
        session.removeAttribute(FLASH_MSG);
        return page(req, type, msg);
    }

    protected PageContext page(HttpServletRequest req, String flashType, String flashMessage) {
        return new PageContext(req.getContextPath(), Csrf.token(req.getSession(true)), currentUser(req),
                flashType, flashMessage, app().auth.googleEnabled() ? app().googleClientId : "");
    }

    protected void flash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_TYPE, type);
        session.setAttribute(FLASH_MSG, message);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void html(HttpServletResponse resp, String page) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getOutputStream().write(page.getBytes(StandardCharsets.UTF_8));
    }

    /** Starts a session for the member. Changing the session id prevents session fixation. */
    protected void signIn(HttpServletRequest req, User user) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            session = req.getSession(true);
        } else {
            req.changeSessionId();
        }
        session.setAttribute(SESSION_USER, user.getId());
        session.setMaxInactiveInterval(8 * 60 * 60);
        app().online.signedIn(user.getId());
    }
}
