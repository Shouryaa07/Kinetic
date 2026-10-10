package com.kinetic.service;

/** The verified identity claims from a Google ID token. */
public record GoogleProfile(String subject, String email, String name, String pictureUrl) {
}
