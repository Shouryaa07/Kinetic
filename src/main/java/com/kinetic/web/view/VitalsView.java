package com.kinetic.web.view;

import com.kinetic.model.BodyMetric;
import com.kinetic.model.DailyLog;
import com.kinetic.model.DailyTargets;
import com.kinetic.model.Totals;
import com.kinetic.service.Dashboard;
import com.kinetic.util.Html;
import com.kinetic.web.PageContext;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Daily health: rings, steps, water, sleep and weight. */
public final class VitalsView {
    private VitalsView() {
    }

    private static String meter(String icon, String label, String value, String of, int pct, String color) {
        return "<div class=\"meter\"><div class=\"meter-top\"><span class=\"meter-ico\" style=\"--c:" + color + "\">" + Icons.get(icon)
                + "</span><strong>" + label + "</strong><span class=\"meter-val\">" + value + "<small> " + of + "</small></span></div>"
                + "<div class=\"track\"><i style=\"width:" + pct + "%;background:" + color + "\"></i></div></div>";
    }

    private static String sleepText(int minutes) {
        return minutes == 0 ? "0h" : (minutes / 60) + "h " + String.format("%02d", minutes % 60) + "m";
    }

    /** The "Today" card shared by the dashboard and the health page. */
    public static String todayCard(PageContext p, Dashboard d, boolean withLink) {
        Totals t = d.todayTotals();
        DailyLog log = d.daily();
        int move = DailyTargets.percent(t.calories(), DailyTargets.MOVE_KCAL);
        int ex = DailyTargets.percent(t.minutes(), DailyTargets.EXERCISE_MIN);
        int st = DailyTargets.percent(log.steps(), DailyTargets.STEPS);
        return "<article class=\"card today\"><div class=\"card-head\"><h2>Today</h2>"
                + (withLink ? "<a href=\"" + p.url("/vitals") + "\">Log steps, water, sleep</a>" : "<span class=\"tag\">" + Html.esc(Fmt.longDate(d.today())) + "</span>")
                + "</div><div class=\"today-body\"><div class=\"rings-wrap\">" + Charts.activityRings(move, ex, st)
                + "<ul class=\"ring-legend\"><li><i style=\"background:#ff375f\"></i><span>Move</span><b>" + Fmt.number(t.calories()) + "<small>/" + DailyTargets.MOVE_KCAL + " kcal</small></b></li>"
                + "<li><i style=\"background:#7ee22d\"></i><span>Exercise</span><b>" + t.minutes() + "<small>/" + DailyTargets.EXERCISE_MIN + " min</small></b></li>"
                + "<li><i style=\"background:#22d3ee\"></i><span>Steps</span><b>" + Fmt.number(log.steps()) + "<small>/" + Fmt.number(DailyTargets.STEPS) + "</small></b></li></ul></div>"
                + "<div class=\"meters\">"
                + meter("drop", "Water", Fmt.number(log.waterMl()), "/ " + Fmt.number(DailyTargets.WATER_ML) + " ml", DailyTargets.percent(log.waterMl(), DailyTargets.WATER_ML), "#0ea5e9")
                + meter("moon2", "Sleep", sleepText(log.sleepMin()), "/ 8h", DailyTargets.percent(log.sleepMin(), DailyTargets.SLEEP_MIN), "#8b5cf6")
                + meter("steps", "Steps", Fmt.number(log.steps()), "/ " + Fmt.number(DailyTargets.STEPS), st, "#22d3ee")
                + "</div></div></article>";
    }

    private static List<String> dayLabels(List<DailyLog> week) {
        List<String> out = new ArrayList<>();
        week.forEach(l -> out.add(l.date().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)));
        return out;
    }

    public static String render(PageContext p, Dashboard d) {
        LocalDate today = d.today();
        DailyLog log = d.daily();
        List<DailyLog> week = d.dailyWeek();
        List<String> labels = dayLabels(week);
        String quick = "<div class=\"quick\"><span>Quick add water</span>";
        for (int ml : new int[]{250, 500, 750}) {
            quick += "<form method=\"post\" action=\"" + p.url("/vitals") + "\">" + p.csrfField()
                    + "<input type=\"hidden\" name=\"action\" value=\"water\"><input type=\"hidden\" name=\"ml\" value=\"" + ml
                    + "\"><button class=\"btn btn-ghost btn-sm\" type=\"submit\">+" + ml + " ml</button></form>";
        }
        quick += "</div>";
        String form = "<article class=\"card\"><div class=\"card-head\"><h2>Log your day</h2></div>"
                + "<form method=\"post\" action=\"" + p.url("/vitals") + "\" class=\"stack\">" + p.csrfField()
                + "<input type=\"hidden\" name=\"action\" value=\"daily\">"
                + "<label class=\"field\"><span>Date</span><input type=\"date\" name=\"date\" value=\"" + today + "\" max=\"" + today.plusDays(1) + "\" required></label>"
                + "<div class=\"two\"><label class=\"field\"><span>Steps</span><input type=\"number\" name=\"steps\" min=\"0\" max=\"100000\" placeholder=\"" + log.steps() + "\"></label>"
                + "<label class=\"field\"><span>Water (ml)</span><input type=\"number\" name=\"water\" min=\"0\" max=\"10000\" step=\"50\" placeholder=\"" + log.waterMl() + "\"></label></div>"
                + "<label class=\"field\"><span>Sleep (hours)</span><input type=\"number\" name=\"sleep\" min=\"0\" max=\"24\" step=\"0.25\" placeholder=\"" + String.format(Locale.ENGLISH, "%.1f", log.sleepMin() / 60.0) + "\"></label>"
                + "<p class=\"hint\">Leave a field empty to keep what is already saved for that day.</p>"
                + "<button class=\"btn btn-primary btn-block\" type=\"submit\">" + Icons.get("check") + "Save</button></form>" + quick + "</article>";

        List<Integer> steps = new ArrayList<>();
        List<Integer> water = new ArrayList<>();
        List<Integer> sleep = new ArrayList<>();
        week.forEach(l -> {
            steps.add(l.steps());
            water.add(l.waterMl());
            sleep.add(Math.round(l.sleepMin() / 6f)); // tenths of an hour for the chart
        });
        double avgSteps = week.stream().mapToInt(DailyLog::steps).average().orElse(0);
        double avgSleep = week.stream().filter(l -> l.sleepMin() > 0).mapToInt(DailyLog::sleepMin).average().orElse(0);

        // Weight
        List<BodyMetric> w = d.weights();
        String weightSummary;
        if (w.isEmpty()) {
            weightSummary = "<p class=\"sub\">No weigh-ins yet.</p>";
        } else {
            BodyMetric last = w.get(w.size() - 1);
            BodyMetric first = w.get(0);
            double change = last.weightKg() - first.weightKg();
            weightSummary = "<div class=\"weight-now\"><strong>" + String.format(Locale.ENGLISH, "%.1f", last.weightKg()) + "<small> kg</small></strong>"
                    + (w.size() > 1 ? "<span class=\"delta " + (change <= 0 ? "down" : "up") + "\">" + String.format(Locale.ENGLISH, "%+.1f kg", change)
                    + " since " + Html.esc(Fmt.shortDate(first.date())) + "</span>" : "") + "</div>";
        }
        String weightForm = "<form method=\"post\" action=\"" + p.url("/vitals") + "\" class=\"weight-form\">" + p.csrfField()
                + "<input type=\"hidden\" name=\"action\" value=\"weight\">"
                + "<label class=\"field\"><span>Date</span><input type=\"date\" name=\"date\" value=\"" + today + "\" max=\"" + today.plusDays(1) + "\" required></label>"
                + "<label class=\"field\"><span>Weight (kg)</span><input type=\"number\" name=\"weight\" min=\"25\" max=\"350\" step=\"0.1\" required placeholder=\"72.0\"></label>"
                + "<button class=\"btn btn-primary\" type=\"submit\">" + Icons.get("plus") + "Add</button></form>";

        String stepsCard = "<article class=\"card\"><div class=\"card-head\"><h2>Steps this week</h2><span class=\"tag\">avg " + Fmt.number(Math.round(avgSteps)) + " a day</span></div>"
                + Charts.dayBars(labels, steps, DailyTargets.STEPS, "#22d3ee", "steps") + "<p class=\"hint\">Dashed line: " + Fmt.number(DailyTargets.STEPS) + " steps goal</p></article>";
        String body = "<section class=\"grid-1\">" + todayCard(p, d, false) + "</section>"
                + "<section class=\"grid-1-2\">" + form + stepsCard + "</section>"
                + "<section class=\"grid-2\">"
                + "<article class=\"card\"><div class=\"card-head\"><h2>Water</h2><span class=\"tag\">goal " + Fmt.number(DailyTargets.WATER_ML) + " ml</span></div>"
                + Charts.dayBars(labels, water, DailyTargets.WATER_ML, "#0ea5e9", "ml") + "</article>"
                + "<article class=\"card\"><div class=\"card-head\"><h2>Sleep</h2><span class=\"tag\">avg " + sleepText((int) Math.round(avgSleep)) + "</span></div>"
                + Charts.dayBars(labels, sleep, 80, "#8b5cf6", "x 6 min") + "<p class=\"hint\">Dashed line: 8 hours</p></article></section>"
                + "<section class=\"card\"><div class=\"card-head\"><h2>Body weight</h2><span class=\"tag\">last 4 months</span></div>"
                + "<div class=\"weight-top\">" + weightSummary + weightForm + "</div>" + Charts.weightLine(w) + "</section>";
        return Layout.app(p, "Health", "vitals", "Steps, water, sleep and weight", body);
    }
}
