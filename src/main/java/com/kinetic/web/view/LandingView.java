package com.kinetic.web.view;

import com.kinetic.service.Dashboard;
import com.kinetic.web.PageContext;
import java.time.LocalDate;
import java.util.List;

/** Public home page. */
public final class LandingView {
    private LandingView() {
    }

    private static String feature(String icon, String title, String text) {
        return "<article class=\"feature\"><div class=\"feature-ico\">" + Icons.get(icon) + "</div><h3>" + title
                + "</h3><p>" + text + "</p></article>";
    }

    public static String render(PageContext p) {
        LocalDate monday = LocalDate.of(2026, 7, 6);
        int[] mins = {95, 140, 120, 175, 150, 210, 185, 230};
        int[] wk = {2, 3, 3, 4, 4, 5, 4, 5};
        List<Dashboard.WeekPoint> sample = new java.util.ArrayList<>();
        for (int i = 0; i < mins.length; i++) {
            sample.add(new Dashboard.WeekPoint(monday.plusWeeks(i), mins[i], wk[i]));
        }
        String body = Layout.publicNav(p)
                + "<section class=\"hero\"><div class=\"hero-copy\"><span class=\"pill\">" + Icons.get("bolt") + "Fitness tracking, simplified</span>"
                + "<h1>Move with <em>intent</em>.<br>See every step of progress.</h1>"
                + "<p class=\"lead\">Kinetic turns every run, ride and lift into clear numbers: weekly trends, streaks and goals you can actually hit.</p>"
                + "<div class=\"hero-cta\"><a class=\"btn btn-primary btn-lg\" href=\"" + p.url("/register") + "\">Create free account</a>"
                + "<a class=\"btn btn-ghost btn-lg\" href=\"" + p.url("/auth/demo") + "\">Try the live demo</a></div>"
                + "<ul class=\"hero-points\"><li>" + Icons.get("check") + "Sign in with Google</li><li>" + Icons.get("check") + "7 workout types</li><li>" + Icons.get("check") + "Weekly goals</li></ul></div>"
                + "<div class=\"hero-art\" aria-hidden=\"true\"><div class=\"card mock\"><div class=\"mock-head\"><strong>This week</strong><span class=\"tag\">+18% vs last week</span></div>"
                + "<div class=\"mock-stats\"><div><b>5</b><small>workouts</small></div><div><b>3h 50m</b><small>active</small></div><div><b>2,140</b><small>kcal</small></div></div>"
                + Charts.weeklyBars(sample) + "</div>"
                + "<div class=\"card mock-float\"><div class=\"ring-wrap\">" + Charts.ring(86, "var(--brand)", "86%") + "</div><div><strong>Weekly goal</strong><small>4 of 5 sessions</small></div></div></div></section>"
                + "<section class=\"features\"><h2>Everything you need, nothing you don't</h2><div class=\"feature-grid\">"
                + feature("activity", "Log in seconds", "Pick a workout type, enter time and effort. Calories are estimated from your weight and the activity.")
                + feature("trend", "Trends that make sense", "Weekly bars, a 17-week activity calendar and personal records show how you are improving.")
                + feature("target", "Goals that stick", "Set weekly targets for sessions, minutes, calories or distance and watch the rings fill.")
                + feature("flame", "Streaks", "Keep a daily streak alive. Kinetic counts consecutive active days and tracks your best run.")
                + feature("shield", "Private by design", "Salted password hashing, CSRF protection and Google sign-in with verified tokens.")
                + feature("download", "Your data, yours", "Export every workout to CSV whenever you like.") + "</div></section>"
                + "<footer class=\"pub-foot\"><span>" + Icons.logo(20) + " Kinetic · Built with Java Servlets and JDBC</span></footer>";
        return Layout.publicPage(p, "Fitness tracking, simplified", "pub landing", body);
    }
}
