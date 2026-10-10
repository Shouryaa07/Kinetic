package com.kinetic.web.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Number and date formatting shared by the views. */
public final class Fmt {
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH);

    private Fmt() {
    }

    public static String minutes(int m) {
        return m >= 60 ? (m / 60) + "h " + String.format("%02d", m % 60) + "m" : m + "m";
    }

    public static String number(double v) {
        return v == Math.rint(v) ? String.format(Locale.ENGLISH, "%,.0f", v) : String.format(Locale.ENGLISH, "%,.1f", v);
    }

    public static String shortDate(LocalDate d) {
        return SHORT.format(d);
    }

    public static String longDate(LocalDate d) {
        return LONG.format(d);
    }

    /** "Today", "Yesterday", or a short date. */
    public static String relative(LocalDate d, LocalDate today) {
        if (d.equals(today)) {
            return "Today";
        }
        if (d.equals(today.minusDays(1))) {
            return "Yesterday";
        }
        return shortDate(d);
    }

    public static String delta(double now, double before) {
        if (before == 0) {
            return now == 0 ? "No change" : "New this week";
        }
        double pct = (now - before) / before * 100;
        return String.format(Locale.ENGLISH, "%s%.0f%% vs last week", pct >= 0 ? "+" : "", pct);
    }
}
