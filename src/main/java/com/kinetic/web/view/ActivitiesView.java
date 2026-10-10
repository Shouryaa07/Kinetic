package com.kinetic.web.view;

import com.kinetic.model.Activity;
import com.kinetic.model.ActivityType;
import com.kinetic.web.PageContext;
import java.time.LocalDate;
import java.util.List;

/** Workout log: entry form on one side, history with filters on the other. */
public final class ActivitiesView {
    private ActivitiesView() {
    }

    public static String render(PageContext p, List<Activity> list, ActivityType filter, LocalDate today, Activity editing) {
        StringBuilder chips = new StringBuilder();
        for (ActivityType t : ActivityType.values()) {
            chips.append("<label class=\"type-chip\" style=\"--c:").append(t.color()).append("\"><input type=\"radio\" name=\"type\" value=\"")
                    .append(t.name()).append("\" data-distance=\"").append(t.distanceBased() ? "1" : "0").append("\"")
                    .append(editing != null && editing.type() == t ? " checked" : "").append(" required><span>")
                    .append(t.label()).append("</span></label>");
        }
        StringBuilder filters = new StringBuilder("<nav class=\"filters\" aria-label=\"Filter by type\"><a class=\"fchip")
                .append(filter == null ? " on" : "").append("\" href=\"").append(p.url("/activities")).append("\">All</a>");
        for (ActivityType t : ActivityType.values()) {
            filters.append("<a class=\"fchip").append(t == filter ? " on" : "").append("\" href=\"").append(p.url("/activities?type=" + t.name()))
                    .append("\">").append(t.label()).append("</a>");
        }
        filters.append("</nav>");

        StringBuilder rows = new StringBuilder();
        if (list.isEmpty()) {
            rows.append("<div class=\"empty\"><p>Nothing here yet. Log a workout with the form.</p></div>");
        } else {
            rows.append("<ul class=\"act-list\">");
            list.forEach(a -> rows.append(DashboardView.activityRow(p, a, today, true)));
            rows.append("</ul>");
        }

        LocalDate shown = editing == null ? today : editing.date();
        String durationValue = editing == null ? "" : String.valueOf(editing.durationMinutes());
        String distanceValue = editing == null || editing.distanceKm() <= 0 ? "" : String.format(java.util.Locale.ENGLISH, "%.2f", editing.distanceKm());
        int effort = editing == null ? 3 : editing.intensity();
        String[] effortLabels = {"", "Easy", "Light", "Moderate", "Hard", "All out"};
        String notesValue = editing == null || editing.notes() == null ? "" : com.kinetic.util.Html.esc(editing.notes());
        String form = "<article class=\"card\" id=\"log\"><div class=\"card-head\"><h2>" + (editing == null ? "Log a workout" : "Edit workout") + "</h2>" + (editing == null ? "" : "<a href=\"" + p.url("/activities") + "\">Cancel</a>") + "</div>"
                + "<form method=\"post\" action=\"" + p.url("/activities") + "\" class=\"stack\">" + p.csrfField()
                + "<input type=\"hidden\" name=\"action\" value=\"" + (editing == null ? "add" : "update") + "\">"
                + (editing == null ? "" : "<input type=\"hidden\" name=\"id\" value=\"" + editing.getId() + "\">")
                + "<fieldset class=\"types\"><legend>Type</legend><div class=\"chips\">" + chips + "</div></fieldset>"
                + "<div class=\"two\"><label class=\"field\"><span>Date <output data-date-out>" + Fmt.longDate(shown) + "</output></span><input type=\"date\" name=\"date\" value=\"" + shown + "\" max=\"" + today.plusDays(1) + "\" required data-date></label>"
                + "<label class=\"field\"><span>Duration (min)</span><input type=\"number\" name=\"duration\" min=\"1\" max=\"1440\" placeholder=\"45\" value=\"" + durationValue + "\" required></label></div>"
                + "<label class=\"field\" data-distance-field" + (editing != null && editing.type().distanceBased() ? "" : " hidden") + "><span>Distance (km)</span><input type=\"number\" name=\"distance\" min=\"0\" max=\"1000\" step=\"0.01\" placeholder=\"5.0\" value=\"" + distanceValue + "\"></label>"
                + "<label class=\"field\"><span>Effort <output data-effort-out>" + effort + " · " + effortLabels[effort] + "</output></span>"
                + "<input type=\"range\" name=\"intensity\" min=\"1\" max=\"5\" value=\"" + effort + "\" data-effort></label>"
                + "<label class=\"field\"><span>Notes (optional)</span><input type=\"text\" name=\"notes\" maxlength=\"500\" placeholder=\"Felt strong on the hills\" value=\"" + notesValue + "\"></label>"
                + "<button class=\"btn btn-primary btn-block\" type=\"submit\">" + Icons.get("check") + (editing == null ? "Save workout" : "Update workout") + "</button></form></article>";

        String history = "<article class=\"card\"><div class=\"card-head\"><h2>History</h2><a class=\"btn btn-quiet btn-sm\" href=\"" + p.url("/export.csv") + "\">"
                + Icons.get("download") + "Export CSV</a></div>" + filters + rows + "</article>";
        return Layout.app(p, "Activities", "activities", "Log and review every session", "<section class=\"grid-1-2\">" + form + history + "</section>");
    }
}
