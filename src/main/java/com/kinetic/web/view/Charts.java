package com.kinetic.web.view;

import com.kinetic.model.ActivityType;
import com.kinetic.model.BodyMetric;
import com.kinetic.service.Dashboard;
import com.kinetic.util.Html;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Server-rendered SVG charts. Colours come from CSS classes so they follow the light/dark theme. */
public final class Charts {
    private Charts() {
    }

    /** Weekly active minutes as vertical bars; the last bar is the current week. */
    public static String weeklyBars(List<Dashboard.WeekPoint> weeks) {
        int w = 720;
        int h = 250;
        int left = 40;
        int bottom = 30;
        int top = 12;
        int max = weeks.stream().mapToInt(Dashboard.WeekPoint::minutes).max().orElse(0);
        int scaleMax = Math.max(60, (int) Math.ceil(max / 60.0) * 60);
        int plotH = h - bottom - top;
        double slot = (double) (w - left) / weeks.size();
        double barW = slot * 0.58;
        StringBuilder sb = new StringBuilder();
        sb.append("<svg class=\"chart\" viewBox=\"0 0 ").append(w).append(' ').append(h)
                .append("\" role=\"img\" aria-label=\"Active minutes per week\">");
        int ticks = 4;
        for (int i = 0; i <= ticks; i++) {
            double y = top + plotH - plotH * i / ticks;
            int value = scaleMax * i / ticks;
            sb.append(String.format(Locale.ENGLISH, "<line class=\"grid\" x1=\"%d\" x2=\"%d\" y1=\"%.1f\" y2=\"%.1f\"/>", left, w, y, y));
            sb.append(String.format(Locale.ENGLISH, "<text class=\"axis\" x=\"%d\" y=\"%.1f\" text-anchor=\"end\">%s</text>",
                    left - 8, y + 4, value >= 60 ? (value / 60.0 == Math.rint(value / 60.0) ? (value / 60) + "h" : String.format(Locale.ENGLISH, "%.1fh", value / 60.0)) : "0"));
        }
        for (int i = 0; i < weeks.size(); i++) {
            Dashboard.WeekPoint p = weeks.get(i);
            double bh = plotH * (double) p.minutes() / scaleMax;
            double x = left + i * slot + (slot - barW) / 2;
            double y = top + plotH - bh;
            boolean current = i == weeks.size() - 1;
            if (p.minutes() > 0) {
                sb.append(String.format(Locale.ENGLISH,
                        "<rect class=\"bar%s\" x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" rx=\"5\"><title>Week of %s: %d min, %d workouts</title></rect>",
                        current ? " current" : "", x, y, barW, bh, Html.esc(Fmt.shortDate(p.start())), p.minutes(), p.workouts()));
            }
            if (weeks.size() <= 8 || i % 2 == weeks.size() % 2 || current) {
                sb.append(String.format(Locale.ENGLISH, "<text class=\"axis\" x=\"%.1f\" y=\"%d\" text-anchor=\"middle\">%s</text>",
                        x + barW / 2, h - 8, Html.esc(Fmt.shortDate(p.start()))));
            }
        }
        return sb.append("</svg>").toString();
    }

    /** Donut of minutes per activity type with the total in the middle. */
    public static String donut(Map<ActivityType, Integer> minutesByType) {
        int total = minutesByType.values().stream().mapToInt(Integer::intValue).sum();
        double r = 52;
        double circ = 2 * Math.PI * r;
        StringBuilder sb = new StringBuilder("<svg class=\"donut\" viewBox=\"0 0 140 140\" role=\"img\" aria-label=\"Time by activity type\">");
        sb.append("<circle class=\"donut-track\" cx=\"70\" cy=\"70\" r=\"52\" fill=\"none\" stroke-width=\"16\"/>");
        double offset = 0;
        if (total > 0) {
            for (Map.Entry<ActivityType, Integer> e : minutesByType.entrySet()) {
                double len = circ * e.getValue() / total;
                sb.append(String.format(Locale.ENGLISH,
                        "<circle cx=\"70\" cy=\"70\" r=\"52\" fill=\"none\" stroke=\"%s\" stroke-width=\"16\" stroke-dasharray=\"%.2f %.2f\" stroke-dashoffset=\"%.2f\" transform=\"rotate(-90 70 70)\"><title>%s: %d min</title></circle>",
                        e.getKey().color(), Math.max(0, len - 2), circ - Math.max(0, len - 2), -offset, e.getKey().label(), e.getValue()));
                offset += len;
            }
        }
        sb.append("<text class=\"donut-num\" x=\"70\" y=\"69\" text-anchor=\"middle\">").append(Fmt.minutes(total))
                .append("</text><text class=\"donut-sub\" x=\"70\" y=\"86\" text-anchor=\"middle\">last 30 days</text></svg>");
        return sb.toString();
    }

    /** Circular progress for a goal. */
    public static String ring(int percent, String color, String centerText) {
        double r = 34;
        double circ = 2 * Math.PI * r;
        double filled = circ * Math.min(100, percent) / 100.0;
        return String.format(Locale.ENGLISH,
                "<svg class=\"ring\" viewBox=\"0 0 84 84\" role=\"img\" aria-label=\"%d percent\">"
                        + "<circle class=\"ring-track\" cx=\"42\" cy=\"42\" r=\"34\" fill=\"none\" stroke-width=\"8\"/>"
                        + "<circle cx=\"42\" cy=\"42\" r=\"34\" fill=\"none\" stroke=\"%s\" stroke-width=\"8\" stroke-linecap=\"round\" "
                        + "stroke-dasharray=\"%.2f %.2f\" transform=\"rotate(-90 42 42)\"/>"
                        + "<text class=\"ring-num\" x=\"42\" y=\"47\" text-anchor=\"middle\">%s</text></svg>",
                percent, color, filled, circ - filled, Html.esc(centerText));
    }

    /** Calendar heatmap: one column per week (Monday first), shaded by active minutes. */
    public static String heatmap(Map<LocalDate, Integer> minutesByDay, LocalDate today, int weeks) {
        int cell = 15;
        int gap = 4;
        int left = 26;
        int top = 18;
        LocalDate start = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .minusWeeks(weeks - 1);
        int w = left + weeks * (cell + gap);
        int h = top + 7 * (cell + gap);
        StringBuilder sb = new StringBuilder();
        sb.append("<svg class=\"heat\" viewBox=\"0 0 ").append(w).append(' ').append(h)
                .append("\" role=\"img\" aria-label=\"Daily activity over the last ").append(weeks).append(" weeks\">");
        String[] dayNames = {"Mon", "", "Wed", "", "Fri", "", ""};
        for (int d = 0; d < 7; d++) {
            if (!dayNames[d].isEmpty()) {
                sb.append("<text class=\"axis\" x=\"0\" y=\"").append(top + d * (cell + gap) + 12).append("\">")
                        .append(dayNames[d]).append("</text>");
            }
        }
        int lastMonth = -1;
        for (int wk = 0; wk < weeks; wk++) {
            LocalDate monday = start.plusWeeks(wk);
            if (monday.getMonthValue() != lastMonth && monday.getDayOfMonth() <= 7) {
                lastMonth = monday.getMonthValue();
                sb.append("<text class=\"axis\" x=\"").append(left + wk * (cell + gap)).append("\" y=\"10\">")
                        .append(monday.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)).append("</text>");
            }
            for (int d = 0; d < 7; d++) {
                LocalDate date = monday.plusDays(d);
                if (date.isAfter(today)) {
                    continue;
                }
                int m = minutesByDay.getOrDefault(date, 0);
                int level = m == 0 ? 0 : m < 30 ? 1 : m < 60 ? 2 : m < 90 ? 3 : 4;
                sb.append(String.format(Locale.ENGLISH,
                        "<rect class=\"cell lv%d\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"4\"><title>%s: %s</title></rect>",
                        level, left + wk * (cell + gap), top + d * (cell + gap), cell, cell,
                        Html.esc(Fmt.shortDate(date)), m == 0 ? "rest day" : m + " min"));
            }
        }
        return sb.append("</svg>").toString();
    }

    /** Three concentric rings in the style of an activity tracker: move, exercise, steps. */
    public static String activityRings(int movePct, int exercisePct, int stepsPct) {
        String[] colors = {"#ff375f", "#7ee22d", "#22d3ee"};
        int[] pcts = {movePct, exercisePct, stepsPct};
        double[] radii = {58, 45, 32};
        StringBuilder sb = new StringBuilder("<svg class=\"rings\" viewBox=\"0 0 140 140\" role=\"img\" aria-label=\"Today's rings: move ")
                .append(movePct).append("%, exercise ").append(exercisePct).append("%, steps ").append(stepsPct).append("%\">");
        for (int i = 0; i < 3; i++) {
            double circ = 2 * Math.PI * radii[i];
            double filled = circ * Math.min(100, pcts[i]) / 100.0;
            sb.append(String.format(Locale.ENGLISH,
                    "<circle class=\"ring-track\" cx=\"70\" cy=\"70\" r=\"%.0f\" fill=\"none\" stroke-width=\"10\" opacity=\".55\"/>"
                            + "<circle cx=\"70\" cy=\"70\" r=\"%.0f\" fill=\"none\" stroke=\"%s\" stroke-width=\"10\" stroke-linecap=\"round\" "
                            + "stroke-dasharray=\"%.2f %.2f\" transform=\"rotate(-90 70 70)\"/>",
                    radii[i], radii[i], colors[i], Math.max(filled, pcts[i] > 0 ? 0.1 : 0), circ - filled));
        }
        return sb.append("</svg>").toString();
    }

    /** Small bar chart for one value per day with a dashed goal line. */
    public static String dayBars(List<String> labels, List<Integer> values, int goal, String color, String unit) {
        int w = 420;
        int h = 170;
        int left = 6;
        int bottom = 26;
        int top = 10;
        int max = Math.max(goal, values.stream().mapToInt(Integer::intValue).max().orElse(0));
        double plotH = h - bottom - top;
        double slot = (double) (w - left) / values.size();
        double barW = slot * 0.56;
        StringBuilder sb = new StringBuilder("<svg class=\"chart\" viewBox=\"0 0 ").append(w).append(' ').append(h)
                .append("\" role=\"img\" aria-label=\"Last 7 days\">");
        double goalY = top + plotH - plotH * goal / max;
        sb.append(String.format(Locale.ENGLISH, "<line class=\"goal-line\" x1=\"%d\" x2=\"%d\" y1=\"%.1f\" y2=\"%.1f\"/>", left, w, goalY, goalY));
        for (int i = 0; i < values.size(); i++) {
            double bh = plotH * values.get(i) / max;
            double x = left + i * slot + (slot - barW) / 2;
            if (values.get(i) > 0) {
                sb.append(String.format(Locale.ENGLISH,
                        "<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" rx=\"5\" fill=\"%s\" opacity=\"%s\"><title>%s: %,d %s</title></rect>",
                        x, top + plotH - bh, barW, bh, color, values.get(i) >= goal ? "1" : ".55", Html.esc(labels.get(i)), values.get(i), unit));
            }
            sb.append(String.format(Locale.ENGLISH, "<text class=\"axis\" x=\"%.1f\" y=\"%d\" text-anchor=\"middle\">%s</text>",
                    x + barW / 2, h - 8, Html.esc(labels.get(i))));
        }
        return sb.append("</svg>").toString();
    }

    /** Weight over time: area under a line, with a dot for every weigh-in. */
    public static String weightLine(List<BodyMetric> points) {
        if (points.size() < 2) {
            return "<div class=\"empty\"><p>Add at least two weigh-ins to see a trend.</p></div>";
        }
        int w = 720;
        int h = 230;
        int left = 44;
        int right = 14;
        int top = 14;
        int bottom = 28;
        double min = points.stream().mapToDouble(BodyMetric::weightKg).min().orElse(0);
        double max = points.stream().mapToDouble(BodyMetric::weightKg).max().orElse(0);
        double pad = Math.max(0.5, (max - min) * 0.2);
        double lo = Math.floor(min - pad);
        double hi = Math.ceil(max + pad);
        long t0 = points.get(0).date().toEpochDay();
        long t1 = points.get(points.size() - 1).date().toEpochDay();
        double plotW = w - left - right;
        double plotH = h - top - bottom;
        StringBuilder line = new StringBuilder();
        StringBuilder dots = new StringBuilder();
        for (BodyMetric m : points) {
            double x = left + (t1 == t0 ? 0 : plotW * (m.date().toEpochDay() - t0) / (t1 - t0));
            double y = top + plotH - plotH * (m.weightKg() - lo) / (hi - lo);
            line.append(String.format(Locale.ENGLISH, "%.1f,%.1f ", x, y));
            dots.append(String.format(Locale.ENGLISH, "<circle class=\"pt\" cx=\"%.1f\" cy=\"%.1f\" r=\"4\"><title>%s: %.1f kg</title></circle>",
                    x, y, Html.esc(Fmt.shortDate(m.date())), m.weightKg()));
        }
        StringBuilder sb = new StringBuilder("<svg class=\"chart\" viewBox=\"0 0 ").append(w).append(' ').append(h)
                .append("\" role=\"img\" aria-label=\"Weight over time\">");
        for (int i = 0; i <= 3; i++) {
            double v = lo + (hi - lo) * i / 3;
            double y = top + plotH - plotH * i / 3;
            sb.append(String.format(Locale.ENGLISH, "<line class=\"grid\" x1=\"%d\" x2=\"%d\" y1=\"%.1f\" y2=\"%.1f\"/>"
                    + "<text class=\"axis\" x=\"%d\" y=\"%.1f\" text-anchor=\"end\">%.1f</text>", left, w - right, y, y, left - 8, y + 4, v));
        }
        String[] first = line.toString().trim().split(" ");
        String lastPt = first[first.length - 1];
        double lastX = Double.parseDouble(lastPt.split(",")[0]);
        double firstX = Double.parseDouble(first[0].split(",")[0]);
        sb.append(String.format(Locale.ENGLISH, "<polygon class=\"area\" points=\"%s %.1f,%.1f %.1f,%.1f\"/>",
                line.toString().trim(), lastX, top + plotH, firstX, top + plotH));
        sb.append("<polyline class=\"trend\" fill=\"none\" points=\"").append(line.toString().trim()).append("\"/>").append(dots);
        sb.append(String.format(Locale.ENGLISH, "<text class=\"axis\" x=\"%d\" y=\"%d\">%s</text><text class=\"axis\" x=\"%d\" y=\"%d\" text-anchor=\"end\">%s</text>",
                left, h - 6, Html.esc(Fmt.shortDate(points.get(0).date())), w - right, h - 6, Html.esc(Fmt.shortDate(points.get(points.size() - 1).date()))));
        return sb.append("</svg>").toString();
    }
}
