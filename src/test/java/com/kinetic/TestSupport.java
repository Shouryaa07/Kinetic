package com.kinetic;

import com.kinetic.db.Database;
import com.kinetic.exception.AuthException;
import com.kinetic.service.GoogleProfile;
import com.kinetic.service.GoogleTokenVerifier;
import java.util.UUID;

/** Builds an isolated in-memory application for each test. */
public final class TestSupport {
    private TestSupport() {
    }

    /** Fake Google verifier: the token "good" is accepted, anything else is rejected. */
    public static final GoogleTokenVerifier FAKE_GOOGLE = new GoogleTokenVerifier() {
        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public GoogleProfile verify(String idToken) throws AuthException {
            if ("good".equals(idToken)) {
                return new GoogleProfile("sub-123", "Pat@Gmail.com".toLowerCase(), "Pat Lee", "https://example.com/p.png");
            }
            throw new AuthException("Google rejected the sign-in token.");
        }
    };

    public static AppContext newApp() {
        Database db = new Database("jdbc:h2:mem:t" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1", "sa", "", 4);
        db.initSchema();
        return new AppContext(db, FAKE_GOOGLE, "test-client", false);
    }
}
