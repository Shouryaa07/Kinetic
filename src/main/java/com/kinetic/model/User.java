package com.kinetic.model;

import java.time.LocalDateTime;

/** A registered member. Google-only accounts have no password hash. */
public class User implements Entity {
    private Long id;
    private String email;
    private String displayName;
    private String passwordHash;
    private AuthProvider provider;
    private String googleSub;
    private String avatarUrl;
    private double weightKg = 70;
    private int heightCm = 170;
    private LocalDateTime createdAt;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public int getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(int heightCm) {
        this.heightCm = heightCm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String firstName() {
        String name = displayName == null ? "" : displayName.trim();
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    public String initials() {
        StringBuilder sb = new StringBuilder();
        for (String part : displayName.trim().split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return sb.toString();
    }

    public double bmi() {
        double m = heightCm / 100.0;
        return m <= 0 ? 0 : weightKg / (m * m);
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isEmpty();
    }
}
