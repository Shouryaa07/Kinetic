package com.kinetic.web.servlet;

import com.kinetic.model.Activity;
import com.kinetic.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Streams all of a member's workouts as a CSV download. */
@WebServlet(name = "ExportServlet", urlPatterns = "/export.csv")
public class ExportServlet extends BaseServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = currentUser(req);
        resp.setContentType("text/csv");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Content-Disposition", "attachment; filename=\"kinetic-workouts.csv\"");
        PrintWriter out = resp.getWriter();
        out.println("date,type,duration_min,distance_km,intensity,calories,notes");
        for (Activity a : app().activities.list(user.getId(), null, 100_000)) {
            out.println(String.join(",", a.date().toString(), a.type().name(), String.valueOf(a.durationMinutes()),
                    String.format(Locale.ENGLISH, "%.2f", a.distanceKm()), String.valueOf(a.intensity()),
                    String.valueOf(a.caloriesBurned()), csv(a.notes())));
        }
    }

    /** Quotes a field and defuses spreadsheet formulas (=, +, -, @ at the start). */
    static String csv(String v) {
        if (v == null) {
            return "";
        }
        String safe = !v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0 ? "'" + v : v;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
