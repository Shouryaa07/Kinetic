package com.kinetic.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.kinetic.exception.AuthException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Tests the claim checks without calling Google. */
public class GoogleVerifierTest {
    private final TokenInfoVerifier verifier = new TokenInfoVerifier("my-client");
    private final long now = 1_800_000_000L;

    private Map<String, String> claims() {
        Map<String, String> c = new HashMap<>();
        c.put("aud", "my-client");
        c.put("iss", "https://accounts.google.com");
        c.put("exp", String.valueOf(now + 600));
        c.put("email_verified", "true");
        c.put("email", "Pat@Gmail.com");
        c.put("sub", "1234");
        c.put("name", "Pat Lee");
        return c;
    }

    @Test
    public void validClaimsProduceProfile() throws Exception {
        GoogleProfile p = verifier.toProfile(claims(), now);
        assertEquals("pat@gmail.com", p.email());
        assertEquals("1234", p.subject());
    }

    @Test
    public void wrongAudienceIsRejected() {
        Map<String, String> c = claims();
        c.put("aud", "someone-else");
        assertThrows(AuthException.class, () -> verifier.toProfile(c, now));
    }

    @Test
    public void expiredUnverifiedAndForeignIssuerAreRejected() {
        Map<String, String> expired = claims();
        expired.put("exp", String.valueOf(now - 1));
        assertThrows(AuthException.class, () -> verifier.toProfile(expired, now));
        Map<String, String> unverified = claims();
        unverified.put("email_verified", "false");
        assertThrows(AuthException.class, () -> verifier.toProfile(unverified, now));
        Map<String, String> issuer = claims();
        issuer.put("iss", "https://evil.example");
        assertThrows(AuthException.class, () -> verifier.toProfile(issuer, now));
    }

    @Test
    public void unconfiguredVerifierRefuses() {
        TokenInfoVerifier none = new TokenInfoVerifier("");
        assertThrows(AuthException.class, () -> none.verify("anything"));
    }
}
