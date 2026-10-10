package com.kinetic.web.view;

import com.kinetic.model.Activity;
import com.kinetic.model.ActivityType;
import com.kinetic.model.GoalProgress;
import com.kinetic.model.Totals;
import com.kinetic.service.Dashboard;
import com.kinetic.util.Html;
import com.kinetic.web.PageContext;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** The signed-in home screen. */
public final class DashboardView {
    private DashboardView() {
    }

    static String stat(String icon, String tone, String label, String value, String note) {
        return "<article class=\"card stat\"><div class=\"stat-ico " + tone + "\">" + Icons.get(icon) + "</div><div><small>"
                + label + "</small><strong>" + value + "</strong><span class=\"note\">" + note + "</span></div></article>";
    }

    static String activityRow(PageContext p, Activity a, java.time.LocalDate today, boolean deletable) {
        ActivityType t = a.type();
        String edit = deletable
                ? "<a class=\"icon-btn edit\" href=\"" + p.url("/activities?edit=" + a.getId()) + "#log\" aria-label=\"Edit workout\">" + Icons.get("edit") + "</a>"
                : "";
        String del = deletable
                ? "<form method=\"post\" action=\"" + p.url("/activities") + "\" data-confirm=\"Delete this workout?\">" + p.csrfField()
                + "<input type=\"hidden\" name=\"action\" value=\"delete\"><input type=\"hidden\" name=\"id\" value=\"" + a.getId()
                + "\"><button class=\"icon-btn danger\" type=\"submit\" aria-label=\"Delete workout\">" + Icons.get("trash") + "</button></form>"
                : "";
        return "<li class=\"act\"><span class=\"dot\" style=\"--c:" + t.color() + "\">" + Html.esc(t.label().substring(0, 1)) + "</span>"
                + "<div class=\"act-main\"><strong>" + Html.esc(t.label()) + "</strong><small>" + Html.esc(a.summary())
                + (a.notes() == null ? "" : " · " + Html.esc(a.notes())) + "</small></div>"
                + "<div class=\"act-meta\"><span>" + Html.esc(Fmt.relative(a.date(), today)) + "</span><small>" + a.caloriesBurned() + " kcal</small></div>"
                + edit + del + "</li>";
    }

    static String legend(Map<ActivityType, Integer> minutes) {
        StringBuilder sb = new StringBuilder("<ul class=\"legend\">");
        minutes.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(e ->
                sb.append("<li><i style=\"background:").append(e.getKey().color()).append("\"></i>")
                        .append(e.getKey().label()).append("<b>").append(Fmt.minutes(e.getValue())).append("</b></li>"));
        return sb.append("</ul>").toString();
    }

    static String goalCard(GoalProgress g) {
        String color = g.achieved() ? "var(--good)" : "var(--brand)";
        return "<li class=\"goal-mini\">" + Charts.ring(g.percent(), color, g.percent() + "%") + "<div><strong>"
                + g.goal().metric().label() + "</strong><small>" + Fmt.number(g.current()) + " / " + Fmt.number(g.goal().target())
                + " " + g.goal().metric().unit() + "</small></div></li>";
    }

    public static String render(PageContext p, Dashboard d) {
        int hour = LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
        Totals w = d.thisWeek();
        Totals lw = d.lastWeek();
        StringBuilder goals = new StringBuilder();
        if (d.goals().isEmpty()) {
            goals.append("<div class=\"empty\"><p>No goals yet. Set a weekly target to stay on track.</p><a class=\"btn btn-ghost\" href=\"")
                    .append(p.url("/goals")).append("\">Set goals</a></div>");
        } else {
            goals.append("<ul class=\"goal-list\">");
            d.goals().forEach(g -> goals.append(goalCard(g)));
            goals.append("</ul>");
        }
        StringBuilder recent = new StringBuilder();
        if (d.recent().isEmpty()) {
            recent.append("<div class=\"empty\"><p>No workouts logged yet.</p><a class=\"btn btn-primary\" href=\"")
                    .append(p.url("/activities#log")).append("\">Log your first workout</a></div>");
        } else {
            recent.append("<ul class=\"act-list\">");
            d.recent().forEach(a -> recent.append(activityRow(p, a, d.today(), false)));
            recent.append("</ul>");
        }
        List<Dashboard.WeekPoint> twelve = d.weeks().subList(d.weeks().size() - 12, d.weeks().size());
        String streakNote = d.streak() > 0 ? "Best: " + d.bestStreak() + " days" : "Log today to start one";
        String body = "<section class=\"grid-4\">"
                + stat("activity", "t-brand", "Workouts this week", String.valueOf(w.workouts()), Html.esc(Fmt.delta(w.workouts(), lw.workouts())))
                + stat("clock", "t-cyan", "Active time", Fmt.minutes(w.minutes()), Html.esc(Fmt.delta(w.minutes(), lw.minutes())))
                + stat("flame", "t-orange", "Calories burned", Fmt.number(w.calories()), Html.esc(Fmt.delta(w.calories(), lw.calories())))
                + stat("bolt", "t-green", "Current streak", d.streak() + (d.streak() == 1 ? " day" : " days"), streakNote) + "</section>"
                + "<section class=\"grid-1\">" + VitalsView.todayCard(p, d, true) + "</section>"
                + "<section class=\"grid-2-1\"><article class=\"card chart-card\"><div class=\"card-head\"><h2>Weekly activity</h2><span class=\"tag\">minutes per week</span></div>"
                + Charts.weeklyBars(twelve) + "</article>"
                + "<article class=\"card\"><div class=\"card-head\"><h2>This week's goals</h2><a href=\"" + p.url("/goals") + "\">Edit</a></div>" + goals + "</article></section>"
                + "<section class=\"grid-2-1\"><article class=\"card\"><div class=\"card-head\"><h2>Recent workouts</h2><a href=\"" + p.url("/activities") + "\">View all</a></div>" + recent + "</article>"
                + "<article class=\"card\"><div class=\"card-head\"><h2>Where your time goes</h2></div><div class=\"donut-wrap\">"
                + Charts.donut(d.minutesByType()) + legend(d.minutesByType()) + "</div></article></section>";
        return Layout.app(p, greeting + ", " + p.user().firstName(), "dashboard", Fmt.longDate(d.today()), body);
    }
}
