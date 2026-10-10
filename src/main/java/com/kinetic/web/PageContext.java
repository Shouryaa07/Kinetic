package com.kinetic.web;

import com.kinetic.model.User;
import com.kinetic.util.Html;

/** Per-request values every view needs: URL prefix, CSRF token, signed-in member and flash message. */
public record PageContext(String base, String csrf, User user, String flashType, String flashMessage,
                          String googleClientId) {

    public String url(String path) {
        return base + path;
    }

    /** Hidden input carrying the CSRF token. Include it in every POST form. */
    public String csrfField() {
        return "<input type=\"hidden\" name=\"_csrf\" value=\"" + Html.esc(csrf) + "\">";
    }
}
