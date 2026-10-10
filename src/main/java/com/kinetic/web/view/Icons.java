package com.kinetic.web.view;

import java.util.Map;

/** Inline SVG icons (24x24 stroke style) so pages need no icon font or image requests. */
public final class Icons {
    private static final Map<String, String> PATHS = Map.ofEntries(
            Map.entry("grid", "<rect x='3' y='3' width='7' height='7' rx='1.5'/><rect x='14' y='3' width='7' height='7' rx='1.5'/><rect x='14' y='14' width='7' height='7' rx='1.5'/><rect x='3' y='14' width='7' height='7' rx='1.5'/>"),
            Map.entry("activity", "<polyline points='22 12 18 12 15 21 9 3 6 12 2 12'/>"),
            Map.entry("target", "<circle cx='12' cy='12' r='10'/><circle cx='12' cy='12' r='6'/><circle cx='12' cy='12' r='2'/>"),
            Map.entry("trend", "<polyline points='23 6 13.5 15.5 8.5 10.5 1 18'/><polyline points='17 6 23 6 23 12'/>"),
            Map.entry("user", "<path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'/><circle cx='12' cy='7' r='4'/>"),
            Map.entry("plus", "<line x1='12' y1='5' x2='12' y2='19'/><line x1='5' y1='12' x2='19' y2='12'/>"),
            Map.entry("trash", "<polyline points='3 6 5 6 21 6'/><path d='M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6'/><path d='M10 11v6M14 11v6'/><path d='M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2'/>"),
            Map.entry("flame", "<path d='M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.07-2.14-.22-4.05 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.15.43-2.29 1-3a2.5 2.5 0 0 0 2.5 2.5z'/>"),
            Map.entry("clock", "<circle cx='12' cy='12' r='10'/><polyline points='12 6 12 12 16 14'/>"),
            Map.entry("pin", "<path d='M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z'/><circle cx='12' cy='10' r='3'/>"),
            Map.entry("bolt", "<polygon points='13 2 3 14 12 14 11 22 21 10 12 10 13 2'/>"),
            Map.entry("logout", "<path d='M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4'/><polyline points='16 17 21 12 16 7'/><line x1='21' y1='12' x2='9' y2='12'/>"),
            Map.entry("sun", "<circle cx='12' cy='12' r='5'/><line x1='12' y1='1' x2='12' y2='3'/><line x1='12' y1='21' x2='12' y2='23'/><line x1='4.2' y1='4.2' x2='5.6' y2='5.6'/><line x1='18.4' y1='18.4' x2='19.8' y2='19.8'/><line x1='1' y1='12' x2='3' y2='12'/><line x1='21' y1='12' x2='23' y2='12'/><line x1='4.2' y1='19.8' x2='5.6' y2='18.4'/><line x1='18.4' y1='5.6' x2='19.8' y2='4.2'/>"),
            Map.entry("moon", "<path d='M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8z'/>"),
            Map.entry("download", "<path d='M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4'/><polyline points='7 10 12 15 17 10'/><line x1='12' y1='15' x2='12' y2='3'/>"),
            Map.entry("check", "<polyline points='20 6 9 17 4 12'/>"),
            Map.entry("trophy", "<path d='M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0V4z'/><path d='M17 5h3v2a3 3 0 0 1-3 3M7 5H4v2a3 3 0 0 0 3 3'/>"),
            Map.entry("shield", "<path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/>"),
            Map.entry("edit", "<path d='M12 20h9'/><path d='M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z'/>"),
            Map.entry("heart", "<path d='M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8l1 1.1L12 21.2l7.8-7.7 1-1.1a5.5 5.5 0 0 0 0-7.8z'/>"),
            Map.entry("drop", "<path d='M12 2.7s7 7.1 7 12a7 7 0 0 1-14 0c0-4.9 7-12 7-12z'/>"),
            Map.entry("moon2", "<path d='M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8z'/>"),
            Map.entry("steps", "<path d='M4 16c0-3 2-5 4-5s3 2 3 4-1 4-3 4-4-1-4-3z'/><path d='M13 8c0-3 1-5 3-5s4 2 4 5-2 5-4 5-3-2-3-5z'/>"),
            Map.entry("route", "<circle cx='6' cy='19' r='3'/><path d='M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15'/><circle cx='18' cy='5' r='3'/>"));

    private Icons() {
    }

    public static String get(String name) {
        return "<svg class=\"ico\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" "
                + "stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\">"
                + PATHS.getOrDefault(name, "") + "</svg>";
    }

    /** The Kinetic mark: a gradient tile with a bolt. */
    public static String logo(int size) {
        return "<svg class=\"logo-mark\" width=\"" + size + "\" height=\"" + size + "\" viewBox=\"0 0 32 32\" aria-hidden=\"true\">"
                + "<defs><linearGradient id=\"kg\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0\" stop-color=\"#6d5dfc\"/>"
                + "<stop offset=\"1\" stop-color=\"#22d3ee\"/></linearGradient></defs>"
                + "<rect width=\"32\" height=\"32\" rx=\"9\" fill=\"url(#kg)\"/>"
                + "<path d=\"M18.5 5 8 18h7l-1.5 9L24 14h-7z\" fill=\"#fff\"/></svg>";
    }
}
