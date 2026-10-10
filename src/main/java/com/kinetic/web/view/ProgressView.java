package com.kinetic.web.view;

import com.kinetic.model.ActivityType;
import com.kinetic.model.Bests;
import com.kinetic.model.Totals;
import com.kinetic.service.Dashboard;
import com.kinetic.service.StatsService;
import com.kinetic.web.PageContext;

/** Long-term view: lifetime totals, activity calendar, trends and personal records. */
public final class ProgressView {
    private ProgressView() {
    }

    private static String best(String icon, String label, String value) {
        return "<li>" + Icons.get(icon) + "<div><small>" + label + "</small><strong>" + value + "</strong></div></li>";
    }

    public static String render(PageContext p, Dashboard d) {
        Totals t = d.lifetime();
        Bests b = d.bests();
        StringBuilder table = new StringBuilder("<table class=\"table\"><thead><tr><th>Type</th><th>Sessions</th><th>Time</th></tr></thead><tbody>");
        if (d.minutesByType().isEmpty()) {
            table.append("<tr><td colspan=\"3\">No activity in the last 30 days.</td></tr>");
        }
        for (ActivityType type : ActivityType.values()) {
            Integer mins = d.minutesByType().get(type);
            if (mins != null) {
                table.append("<tr><td><i class=\"swatch\" style=\"background:").append(type.color()).append("\"></i>").append(type.label())
                        .append("</td><td>").append(d.sessionsByType().get(type)).append("</td><td>").append(Fmt.minutes(mins)).append("</td></tr>");
            }
        }
        table.append("</tbody></table>");
        String body = "<section class=\"grid-4\">"
                + DashboardView.stat("trophy", "t-brand", "Total workouts", String.valueOf(t.workouts()), "since you joined")
                + DashboardView.stat("clock", "t-cyan", "Time active", Fmt.minutes(t.minutes()), "all time")
                + DashboardView.stat("flame", "t-orange", "Calories burned", Fmt.number(t.calories()), "all time")
                + DashboardView.stat("route", "t-green", "Distance covered", Fmt.number(t.distanceKm()) + " km", "all time") + "</section>"
                + "<section class=\"card\"><div class=\"card-head\"><h2>Activity calendar</h2><div class=\"scale\">Less <i class=\"cell lv0\"></i><i class=\"cell lv1\"></i><i class=\"cell lv2\"></i><i class=\"cell lv3\"></i><i class=\"cell lv4\"></i> More</div></div>"
                + "<div class=\"scroll-x\">" + Charts.heatmap(d.minutesByDay(), d.today(), StatsService.WEEKS) + "</div></section>"
                + "<section class=\"grid-2-1\"><article class=\"card\"><div class=\"card-head\"><h2>Weekly trend</h2><span class=\"tag\">last " + d.weeks().size() + " weeks</span></div>"
                + Charts.weeklyBars(d.weeks()) + "</article>"
                + "<article class=\"card\"><div class=\"card-head\"><h2>Personal records</h2></div><ul class=\"bests\">"
                + best("flame", "Current streak", d.streak() + " days")
                + best("trophy", "Best streak", d.bestStreak() + " days")
                + best("route", "Longest distance", Fmt.number(b.longestKm()) + " km")
                + best("clock", "Longest session", Fmt.minutes(b.longestMinutes()))
                + best("bolt", "Most calories, one session", Fmt.number(b.mostCalories()) + " kcal") + "</ul></article></section>"
                + "<section class=\"card\"><div class=\"card-head\"><h2>Last 30 days by type</h2></div>" + table + "</section>";
        return Layout.app(p, "Progress", "progress", "Your training over time", body);
    }
}
