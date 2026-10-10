package com.kinetic.web;

import com.kinetic.AppContext;
import com.kinetic.DemoData;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import com.kinetic.web.servlet.BaseServlet;
import com.kinetic.util.Config;
import java.util.logging.Logger;

/** Starts the application (database, services, demo data) and stops it cleanly; tracks online members. */
@WebListener
public class AppListener implements ServletContextListener, HttpSessionListener {
    private static final Logger LOG = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        AppContext app = AppContext.fromConfig();
        if (app.demoEnabled) {
            DemoData.seedIfEmpty(app);
        }
        // Behind HTTPS in production set cookie.secure=true so the session cookie never travels over HTTP.
        event.getServletContext().getSessionCookieConfig().setSecure(Config.getBool("cookie.secure", false));
        event.getServletContext().getSessionCookieConfig().setHttpOnly(true);
        event.getServletContext().setAttribute(AppContext.KEY, app);
        LOG.info("Kinetic started. Google sign-in " + (app.auth.googleEnabled() ? "enabled" : "not configured"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Object app = event.getServletContext().getAttribute(AppContext.KEY);
        if (app instanceof AppContext ctx) {
            ctx.close();
        }
    }

    /** A session ending (logout or timeout) removes the member from the online set. */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        Object uid = event.getSession().getAttribute(BaseServlet.SESSION_USER);
        Object app = event.getSession().getServletContext().getAttribute(AppContext.KEY);
        if (uid instanceof Long id && app instanceof AppContext ctx) {
            ctx.online.signedOut(id);
        }
    }
}
