package com.kinetic.util;

import com.kinetic.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Input validation helpers. Each throws {@link ValidationException} naming the bad field. */
public final class Validate {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validate() {
    }

    public static String text(String raw, String field, int min, int max) throws ValidationException {
        String v = raw == null ? "" : raw.trim();
        if (v.length() < min) {
            throw new ValidationException(field, min == 1 ? field + " is required." : field + " must be at least " + min + " characters.");
        }
        if (v.length() > max) {
            throw new ValidationException(field, field + " must be at most " + max + " characters.");
        }
        return v;
    }

    public static String email(String raw) throws ValidationException {
        String v = text(raw, "Email", 3, 190).toLowerCase();
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidationException("Email", "Enter a valid email address.");
        }
        return v;
    }

    public static String password(String raw) throws ValidationException {
        if (raw == null || raw.length() < 8) {
            throw new ValidationException("Password", "Password must be at least 8 characters.");
        }
        if (raw.length() > 100) {
            throw new ValidationException("Password", "Password must be at most 100 characters.");
        }
        boolean letter = raw.chars().anyMatch(Character::isLetter);
        boolean digit = raw.chars().anyMatch(Character::isDigit);
        if (!letter || !digit) {
            throw new ValidationException("Password", "Password must contain a letter and a number.");
        }
        return raw;
    }

    public static int integer(String raw, String field, int min, int max) throws ValidationException {
        try {
            int v = Integer.parseInt(raw == null ? "" : raw.trim());
            return range(v, field, min, max);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " must be a whole number.");
        }
    }

    /** Parses an optional decimal; blank means zero. Accepts a comma as the decimal mark. */
    public static double decimal(String raw, String field, double min, double max) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return range(Double.parseDouble(raw.trim().replace(',', '.')), field, min, max);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " must be a number.");
        }
    }

    public static LocalDate date(String raw, String field) throws ValidationException {
        try {
            return LocalDate.parse(raw == null ? "" : raw.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, "Enter a valid " + field.toLowerCase() + ".");
        }
    }

    private static int range(int v, String field, int min, int max) throws ValidationException {
        if (v < min || v > max) {
            throw new ValidationException(field, field + " must be between " + min + " and " + max + ".");
        }
        return v;
    }

    private static double range(double v, String field, double min, double max) throws ValidationException {
        if (Double.isNaN(v) || v < min || v > max) {
            throw new ValidationException(field, field + " must be between " + min + " and " + max + ".");
        }
        return v;
    }
}
