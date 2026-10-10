package com.kinetic.web.view;

import com.kinetic.util.Html;
import com.kinetic.web.PageContext;

/** The HTML shells: signed-in application frame and the public (marketing and auth) frame. */
public final class Layout {
    private static final String[][] NAV = {
            {"dashboard", "/dashboard", "grid", "Dashboard"},
            {"activities", "/activities", "activity", "Activities"},
            {"goals", "/goals", "target", "Goals"},
            {"vitals", "/vitals", "heart", "Health"},
            {"progress", "/progress", "trend", "Progress"},
            {"profile", "/profile", "user", "Profile"},
    };

    /** Appended to static file URLs so browsers fetch fresh CSS and JS after each release. */
    private static final String V = "?v=1.1.0";

    private Layout() {
    }

    private static String head(PageContext p, String title) {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + Html.esc(title) + " · Kinetic</title>"
                + "<meta name=\"description\" content=\"Kinetic is a fitness tracker for workouts, goals and progress.\">"
                + "<link rel=\"icon\" href=\"" + p.url("/img/favicon.svg") + "\" type=\"image/svg+xml\">"
                + "<link rel=\"manifest\" href=\"" + p.url("/manifest.webmanifest") + "\">"
                + "<link rel=\"apple-touch-icon\" href=\"" + p.url("/img/icon-192.png") + "\">"
                + "<meta name=\"theme-color\" content=\"#5b4df5\"><meta name=\"apple-mobile-web-app-capable\" content=\"yes\">"
                + "<meta name=\"apple-mobile-web-app-title\" content=\"Kinetic\">"
                + "<link rel=\"stylesheet\" href=\"" + p.url("/css/kinetic.css") + V + "\">"
                + "<script src=\"" + p.url("/js/theme.js") + V + "\"></script></head>";
    }

    private static String themeToggle() {
        return "<button type=\"button\" class=\"icon-btn\" data-theme-toggle aria-label=\"Toggle dark mode\">"
                + "<span class=\"when-light\">" + Icons.get("moon") + "</span>"
                + "<span class=\"when-dark\">" + Icons.get("sun") + "</span></button>";
    }

    private static String flash(PageContext p) {
        if (p.flashMessage() == null) {
            return "";
        }
        return "<div class=\"flash flash-" + Html.esc(p.flashType()) + "\" role=\"status\">"
                + Html.esc(p.flashMessage()) + "</div>";
    }

    private static String avatar(PageContext p) {
        String img = p.user().getAvatarUrl();
        if (img != null && img.startsWith("https://")) {
            return "<img class=\"avatar\" src=\"" + Html.esc(img) + "\" alt=\"\" referrerpolicy=\"no-referrer\">";
        }
        return "<span class=\"avatar\">" + Html.esc(p.user().initials()) + "</span>";
    }

    /** Signed-in frame with sidebar (desktop) or bottom tab bar (phone). */
    public static String app(PageContext p, String title, String active, String subtitle, String body) {
        StringBuilder nav = new StringBuilder();
        for (String[] n : NAV) {
            nav.append("<a class=\"nav-link").append(n[0].equals(active) ? " active" : "").append("\" href=\"")
                    .append(p.url(n[1])).append("\">").append(Icons.get(n[2])).append("<span>").append(n[3]).append("</span></a>");
        }
        return head(p, title) + "<body class=\"app\">"
                + "<aside class=\"sidebar\"><a class=\"brand\" href=\"" + p.url("/dashboard") + "\">" + Icons.logo(32)
                + "<span>Kinetic</span></a><nav class=\"nav\" aria-label=\"Main\">" + nav + "</nav>"
                + "<div class=\"side-foot\"><div class=\"who\">" + avatar(p) + "<div><strong>" + Html.esc(p.user().getDisplayName())
                + "</strong><small>" + Html.esc(p.user().getEmail()) + "</small></div></div>"
                + "<form method=\"post\" action=\"" + p.url("/logout") + "\">" + p.csrfField()
                + "<button class=\"btn btn-quiet btn-block\" type=\"submit\">" + Icons.get("logout") + "Sign out</button></form></div></aside>"
                + "<main class=\"main\"><header class=\"topbar\"><div><h1>" + Html.esc(title) + "</h1>"
                + (subtitle == null ? "" : "<p class=\"sub\">" + Html.esc(subtitle) + "</p>") + "</div>"
                + "<div class=\"top-actions\">" + themeToggle()
                + "<a class=\"btn btn-primary\" href=\"" + p.url("/activities#log") + "\">" + Icons.get("plus") + "<span>Log workout</span></a></div></header>"
                + flash(p) + body + "</main>"
                + "<script src=\"" + p.url("/js/app.js") + V + "\" defer></script></body></html>";
    }

    /** Frame for landing, login and register pages. */
    public static String publicPage(PageContext p, String title, String bodyClass, String body) {
        return head(p, title) + "<body class=\"" + bodyClass + "\">" + body
                + "<script src=\"" + p.url("/js/app.js") + V + "\" defer></script></body></html>";
    }

    public static String publicNav(PageContext p) {
        return "<header class=\"pub-nav\"><a class=\"brand\" href=\"" + p.url("/") + "\">" + Icons.logo(30)
                + "<span>Kinetic</span></a><div class=\"pub-actions\">" + themeToggle()
                + "<a class=\"btn btn-quiet\" href=\"" + p.url("/login") + "\">Sign in</a>"
                + "<a class=\"btn btn-primary\" href=\"" + p.url("/register") + "\">Get started</a></div></header>";
    }

    public static String flashHtml(PageContext p) {
        return flash(p);
    }
}
