package com.kinetic.service;

import com.kinetic.exception.AuthException;

/** Turns a Google ID token (a JWT from "Sign in with Google") into a verified profile. */
public interface GoogleTokenVerifier {
    boolean isConfigured();

    GoogleProfile verify(String idToken) throws AuthException;
}
