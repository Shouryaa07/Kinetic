package com.kinetic.service;

import com.kinetic.concurrent.AuditLogger;
import com.kinetic.dao.UserDao;
import com.kinetic.exception.AuthException;
import com.kinetic.exception.KineticException;
import com.kinetic.exception.ValidationException;
import com.kinetic.model.AuthProvider;
import com.kinetic.model.User;
import com.kinetic.util.PasswordHasher;
import com.kinetic.util.Validate;
import java.util.Optional;

/** Registration, password sign-in and Google sign-in. */
public class AuthService {
    public static final String DEMO_EMAIL = "demo@kinetic.app";

    private final UserDao users;
    private final GoogleTokenVerifier google;
    private final AuditLogger audit;
    private final LoginThrottle throttle = new LoginThrottle();

    public AuthService(UserDao users, GoogleTokenVerifier google, AuditLogger audit) {
        this.users = users;
        this.google = google;
        this.audit = audit;
    }

    public boolean googleEnabled() {
        return google.isConfigured();
    }

    public User register(String name, String email, String password) throws KineticException {
        String cleanName = Validate.text(name, "Name", 2, 100);
        String cleanEmail = Validate.email(email);
        Validate.password(password);
        if (users.findByEmail(cleanEmail).isPresent()) {
            throw new ValidationException("Email", "An account with this email already exists.");
        }
        User u = new User();
        u.setDisplayName(cleanName);
        u.setEmail(cleanEmail);
        u.setPasswordHash(PasswordHasher.hash(password));
        u.setProvider(AuthProvider.LOCAL);
        User saved = users.save(u);
        audit.log(saved.getId(), "REGISTER", "local");
        return saved;
    }

    public User login(String email, String password) throws KineticException {
        String key = email == null ? "" : email.trim().toLowerCase();
        long now = System.currentTimeMillis();
        if (throttle.isBlocked(key, now)) {
            audit.log(null, "LOGIN_BLOCKED", key);
            throw new AuthException("Too many failed attempts. Try again in ten minutes.");
        }
        Optional<User> found = users.findByEmail(key);
        // Same message for unknown email and wrong password so accounts cannot be enumerated.
        if (found.isEmpty() || !found.get().hasPassword()
                || !PasswordHasher.verify(password, found.get().getPasswordHash())) {
            if (found.isPresent() && !found.get().hasPassword()) {
                throw new AuthException("This account uses Google. Choose Continue with Google.");
            }
            throttle.failure(key, now);
            audit.log(found.map(User::getId).orElse(null), "LOGIN_FAILED", key);
            throw new AuthException("Incorrect email or password.");
        }
        throttle.success(key);
        audit.log(found.get().getId(), "LOGIN", "password");
        return found.get();
    }

    /** Verifies the Google token, then finds, links or creates the matching member. */
    public User loginWithGoogle(String idToken) throws KineticException {
        GoogleProfile profile = google.verify(idToken);
        return signInGoogleProfile(profile);
    }

    User signInGoogleProfile(GoogleProfile p) {
        Optional<User> bySub = users.findByGoogleSub(p.subject());
        User user;
        if (bySub.isPresent()) {
            user = bySub.get();
        } else {
            Optional<User> byEmail = users.findByEmail(p.email());
            if (byEmail.isPresent()) {
                user = byEmail.get(); // link Google to the existing account (email is verified)
                user.setGoogleSub(p.subject());
                // Anti pre-hijacking: someone may have registered this address with a password they
                // chose before the real owner arrived. Dropping the password means only Google
                // (which proved ownership of the email) can open the account from now on.
                user.setPasswordHash(null);
                user.setDisplayName(p.name());
            } else {
                user = new User();
                user.setEmail(p.email());
                user.setProvider(AuthProvider.GOOGLE);
                user.setGoogleSub(p.subject());
                user.setDisplayName(p.name());
            }
        }
        // The name is only taken from Google on first sign-in, so later edits on the profile page stick.
        user.setAvatarUrl(p.pictureUrl());
        User saved = users.save(user);
        audit.log(saved.getId(), "LOGIN", "google");
        return saved;
    }

    /** The shared demo member, if the seeder created one. */
    public Optional<User> demoUser() {
        return users.findByEmail(DEMO_EMAIL);
    }

    public Optional<User> find(long id) {
        return users.findById(id);
    }

    public User updateProfile(User user, String name, String weight, String height) throws KineticException {
        user.setDisplayName(Validate.text(name, "Name", 2, 100));
        user.setWeightKg(Validate.decimal(weight, "Weight", 25, 350));
        user.setHeightCm(Validate.integer(height, "Height", 100, 250));
        return users.save(user);
    }
}
