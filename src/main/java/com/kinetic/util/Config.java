package com.kinetic.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads settings from application.properties. A JVM property with the same key wins, then an
 * environment variable named KINETIC_ plus the key in upper case (dots become underscores).
 */
public final class Config {
    private static final Properties FILE = load();

    private Config() {
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read application.properties", e);
        }
        return props;
    }

    public static String get(String key, String fallback) {
        String sys = System.getProperty(key);
        if (sys != null) {
            return sys;
        }
        String env = System.getenv("KINETIC_" + key.toUpperCase().replace('.', '_'));
        if (env != null) {
            return env;
        }
        return FILE.getProperty(key, fallback);
    }

    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean getBool(String key, boolean fallback) {
        return Boolean.parseBoolean(get(key, String.valueOf(fallback)).trim());
    }
}
