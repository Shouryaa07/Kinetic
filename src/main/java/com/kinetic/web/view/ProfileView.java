package com.kinetic.web.view;

import com.kinetic.model.AuthProvider;
import com.kinetic.model.User;
import com.kinetic.util.Html;
import com.kinetic.web.PageContext;
import java.util.Locale;

/** Profile settings. Weight feeds the calorie estimate. */
public final class ProfileView {
    private ProfileView() {
    }

    public static String render(PageContext p) {
        User u = p.user();
        String provider = u.getProvider() == AuthProvider.GOOGLE || u.getGoogleSub() != null ? "Google" : "Email and password";
        String body = "<section class=\"grid-2-1\"><article class=\"card\"><div class=\"card-head\"><h2>Your details</h2></div>"
                + "<form method=\"post\" action=\"" + p.url("/profile") + "\" class=\"stack\">" + p.csrfField()
                + "<label class=\"field\"><span>Full name</span><input name=\"name\" value=\"" + Html.esc(u.getDisplayName()) + "\" required maxlength=\"100\"></label>"
                + "<label class=\"field\"><span>Email</span><input value=\"" + Html.esc(u.getEmail()) + "\" disabled></label>"
                + "<div class=\"two\"><label class=\"field\"><span>Weight (kg)</span><input type=\"number\" name=\"weight\" min=\"25\" max=\"350\" step=\"0.1\" value=\""
                + String.format(Locale.ENGLISH, "%.1f", u.getWeightKg()) + "\" required></label>"
                + "<label class=\"field\"><span>Height (cm)</span><input type=\"number\" name=\"height\" min=\"100\" max=\"250\" value=\"" + u.getHeightCm() + "\" required></label></div>"
                + "<p class=\"hint\">Weight is used to estimate calories burned: MET × weight × hours.</p>"
                + "<button class=\"btn btn-primary\" type=\"submit\">" + Icons.get("check") + "Save changes</button></form></article>"
                + "<article class=\"card profile-side\"><div class=\"profile-id\">" + (u.getAvatarUrl() != null && u.getAvatarUrl().startsWith("https://")
                ? "<img class=\"avatar lg\" src=\"" + Html.esc(u.getAvatarUrl()) + "\" alt=\"\" referrerpolicy=\"no-referrer\">"
                : "<span class=\"avatar lg\">" + Html.esc(u.initials()) + "</span>")
                + "<strong>" + Html.esc(u.getDisplayName()) + "</strong><small>Member since " + u.getCreatedAt().toLocalDate().getYear() + "</small></div>"
                + "<dl class=\"facts\"><div><dt>Sign-in method</dt><dd>" + provider + "</dd></div>"
                + "<div><dt>Body mass index</dt><dd>" + String.format(Locale.ENGLISH, "%.1f", u.bmi()) + "</dd></div></dl></article></section>";
        return Layout.app(p, "Profile", "profile", "Manage your account", body);
    }
}
