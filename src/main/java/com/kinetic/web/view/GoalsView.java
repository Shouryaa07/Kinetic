package com.kinetic.web.view;

import com.kinetic.model.GoalMetric;
import com.kinetic.model.GoalProgress;
import com.kinetic.web.PageContext;
import java.util.List;

/** Weekly goal editor with progress rings. */
public final class GoalsView {
    private GoalsView() {
    }

    public static String render(PageContext p, List<GoalProgress> progress) {
        StringBuilder cards = new StringBuilder();
        for (GoalMetric m : GoalMetric.values()) {
            GoalProgress gp = progress.stream().filter(g -> g.goal().metric() == m).findFirst().orElse(null);
            String value = gp == null ? "" : Fmt.number(gp.goal().target()).replace(",", "");
            int pct = gp == null ? 0 : gp.percent();
            String status = gp == null ? "No goal set"
                    : gp.achieved() ? "Goal reached" : Fmt.number(gp.current()) + " of " + Fmt.number(gp.goal().target()) + " " + m.unit();
            cards.append("<article class=\"card goal-card\">")
                    .append(Charts.ring(pct, gp != null && gp.achieved() ? "var(--good)" : "var(--brand)", pct + "%"))
                    .append("<div class=\"goal-body\"><h3>").append(m.label()).append("</h3><small>").append(status).append("</small>")
                    .append("<label class=\"field inline\"><span>Weekly target (").append(m.unit()).append(")</span>")
                    .append("<input type=\"number\" name=\"").append(m.name()).append("\" min=\"0\" step=\"").append(m.step())
                    .append("\" placeholder=\"e.g. ").append(Fmt.number(m.suggested()).replace(",", "")).append("\" value=\"").append(value).append("\"></label></div></article>");
        }
        String body = "<form method=\"post\" action=\"" + p.url("/goals") + "\">" + p.csrfField()
                + "<section class=\"grid-2\">" + cards + "</section>"
                + "<div class=\"form-foot\"><p class=\"hint\">Leave a target empty to remove it. Weeks run Monday to Sunday.</p>"
                + "<button class=\"btn btn-primary\" type=\"submit\">" + Icons.get("check") + "Save goals</button></div></form>";
        return Layout.app(p, "Goals", "goals", "Weekly targets, reset every Monday", body);
    }
}
