package com.kinetic.service;

import com.kinetic.exception.AuthException;
import com.kinetic.util.JsonLite;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * Verifies ID tokens by asking Google's {@code tokeninfo} endpoint, which checks the signature.
 * We then check the claims ourselves: audience must be our client ID, issuer must be Google,
 * the token must be unexpired, and the email must be verified.
 */
public class TokenInfoVerifier implements GoogleTokenVerifier {
    private static final String ENDPOINT = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final String clientId;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public TokenInfoVerifier(String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
    }

    @Override
    public boolean isConfigured() {
        return !clientId.isEmpty();
    }

    @Override
    public GoogleProfile verify(String idToken) throws AuthException {
        if (!isConfigured()) {
            throw new AuthException("Google sign-in is not configured on this server.");
        }
        if (idToken == null || idToken.isBlank() || idToken.length() > 4096) {
            throw new AuthException("Missing Google credential.");
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(
                            URI.create(ENDPOINT + URLEncoder.encode(idToken, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new AuthException("Google rejected the sign-in token.");
            }
            return toProfile(JsonLite.parseObject(res.body()), System.currentTimeMillis() / 1000);
        } catch (IOException e) {
            throw new AuthException("Could not reach Google to verify the sign-in.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthException("Google sign-in was interrupted.", e);
        } catch (IllegalArgumentException e) {
            throw new AuthException("Google returned an unreadable response.", e);
        }
    }

    /** Claim checks, separated from the network call so they can be unit-tested. */
    GoogleProfile toProfile(Map<String, String> claims, long nowEpochSeconds) throws AuthException {
        if (!clientId.equals(claims.get("aud"))) {
            throw new AuthException("This Google token was issued for a different application.");
        }
        if (!ISSUERS.contains(claims.get("iss"))) {
            throw new AuthException("Untrusted token issuer.");
        }
        long exp;
        try {
            exp = Long.parseLong(claims.getOrDefault("exp", "0"));
        } catch (NumberFormatException e) {
            throw new AuthException("Invalid token expiry.");
        }
        if (exp <= nowEpochSeconds) {
            throw new AuthException("The Google sign-in has expired. Try again.");
        }
        if (!"true".equals(claims.get("email_verified"))) {
            throw new AuthException("Your Google email address is not verified.");
        }
        String email = claims.get("email");
        String sub = claims.get("sub");
        if (email == null || sub == null) {
            throw new AuthException("Google did not return an email address.");
        }
        String name = claims.getOrDefault("name", email.substring(0, email.indexOf('@')));
        return new GoogleProfile(sub, email.toLowerCase(), name, claims.get("picture"));
    }
}
