package com.kinetic.web.view;

import com.kinetic.util.Html;
import com.kinetic.web.PageContext;

/** Sign-in and registration pages. */
public final class AuthView {
    private AuthView() {
    }

    private static String googleBlock(PageContext p, boolean demoEnabled, String verb) {
        String button;
        if (p.googleClientId() != null && !p.googleClientId().isBlank()) {
            button = "<div id=\"google-btn\" class=\"google-slot\" data-client-id=\"" + Html.esc(p.googleClientId())
                    + "\" data-action=\"" + p.url("/auth/google") + "\" data-csrf=\"" + Html.esc(p.csrf()) + "\"></div>"
                    + "<script src=\"https://accounts.google.com/gsi/client\" async defer></script>"
                    + "<script src=\"" + p.url("/js/google.js") + "?v=1.1.0" + "\"></script>";
        } else {
            button = "<button type=\"button\" class=\"btn btn-google\" data-google-missing>" + googleG()
                    + "Continue with Google</button><p class=\"hint\" id=\"google-missing\" hidden>Google sign-in needs a client ID. "
                    + "Set <code>KINETIC_GOOGLE_CLIENT_ID</code> and restart (see the README).</p>";
        }
        return button + "<div class=\"divider\"><span>or " + verb + " with email</span></div>";
    }

    private static String googleG() {
        return "<svg width=\"18\" height=\"18\" viewBox=\"0 0 48 48\" aria-hidden=\"true\"><path fill=\"#EA4335\" d=\"M24 9.5c3.5 0 6.6 1.2 9.1 3.6l6.8-6.8C35.8 2.4 30.3 0 24 0 14.6 0 6.5 5.4 2.6 13.2l7.9 6.1C12.4 13.5 17.7 9.5 24 9.5z\"/><path fill=\"#4285F4\" d=\"M46.5 24.5c0-1.6-.1-3.1-.4-4.5H24v9h12.7c-.6 3-2.3 5.5-4.8 7.2l7.5 5.8c4.4-4.1 7.1-10.1 7.1-17.5z\"/><path fill=\"#FBBC05\" d=\"M10.5 28.7a14.5 14.5 0 0 1 0-9.4l-7.9-6.1a24 24 0 0 0 0 21.6l7.9-6.1z\"/><path fill=\"#34A853\" d=\"M24 48c6.5 0 11.9-2.1 15.9-5.8l-7.5-5.8c-2.1 1.4-4.9 2.3-8.4 2.3-6.3 0-11.6-4-13.5-9.8l-7.9 6.1C6.5 42.6 14.6 48 24 48z\"/></svg>";
    }

    private static String side() {
        return "<aside class=\"auth-side\"><div class=\"auth-side-in\">" + Icons.logo(40)
                + "<h2>Train smarter.<br>Stay consistent.</h2><p>Log workouts, hit weekly goals and watch your streak grow.</p>"
                + "<ul><li>" + Icons.get("flame") + "Daily streak tracking</li><li>" + Icons.get("target") + "Weekly goal rings</li>"
                + "<li>" + Icons.get("trend") + "17-week activity calendar</li></ul></div></aside>";
    }

    private static String field(String id, String label, String type, String value, String extra) {
        return "<label class=\"field\" for=\"" + id + "\"><span>" + label + "</span><input id=\"" + id + "\" name=\"" + id
                + "\" type=\"" + type + "\" value=\"" + Html.esc(value) + "\" " + extra + "></label>";
    }

    public static String login(PageContext p, boolean demoEnabled, String email) {
        String demo = demoEnabled ? "<a class=\"btn btn-ghost btn-block\" href=\"" + p.url("/auth/demo") + "\">" + Icons.get("bolt") + "Explore the demo account</a>" : "";
        String body = "<div class=\"auth\">" + side() + "<section class=\"auth-main\"><div class=\"auth-card\">"
                + "<a class=\"brand mobile-brand\" href=\"" + p.url("/") + "\">" + Icons.logo(28) + "<span>Kinetic</span></a>"
                + "<h1>Welcome back</h1><p class=\"sub\">Sign in to continue your training.</p>" + Layout.flashHtml(p)
                + googleBlock(p, demoEnabled, "sign in")
                + "<form method=\"post\" action=\"" + p.url("/login") + "\" class=\"stack\">" + p.csrfField()
                + field("email", "Email", "email", email, "autocomplete=\"email\" required autofocus")
                + field("password", "Password", "password", "", "autocomplete=\"current-password\" required")
                + "<button class=\"btn btn-primary btn-block\" type=\"submit\">Sign in</button></form>" + demo
                + "<p class=\"switch\">New to Kinetic? <a href=\"" + p.url("/register") + "\">Create an account</a></p></div></section></div>";
        return Layout.publicPage(p, "Sign in", "pub", body);
    }

    public static String register(PageContext p, String name, String email) {
        String body = "<div class=\"auth\">" + side() + "<section class=\"auth-main\"><div class=\"auth-card\">"
                + "<a class=\"brand mobile-brand\" href=\"" + p.url("/") + "\">" + Icons.logo(28) + "<span>Kinetic</span></a>"
                + "<h1>Create your account</h1><p class=\"sub\">Free, and ready in under a minute.</p>" + Layout.flashHtml(p)
                + googleBlock(p, false, "sign up")
                + "<form method=\"post\" action=\"" + p.url("/register") + "\" class=\"stack\">" + p.csrfField()
                + field("name", "Full name", "text", name, "autocomplete=\"name\" required maxlength=\"100\" autofocus")
                + field("email", "Email", "email", email, "autocomplete=\"email\" required")
                + field("password", "Password", "password", "", "autocomplete=\"new-password\" required minlength=\"8\"")
                + "<p class=\"hint\">At least 8 characters with a letter and a number.</p>"
                + "<button class=\"btn btn-primary btn-block\" type=\"submit\">Create account</button></form>"
                + "<p class=\"switch\">Already a member? <a href=\"" + p.url("/login") + "\">Sign in</a></p></div></section></div>";
        return Layout.publicPage(p, "Create account", "pub", body);
    }
}
