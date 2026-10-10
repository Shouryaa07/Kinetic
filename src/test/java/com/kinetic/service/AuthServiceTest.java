package com.kinetic.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.kinetic.AppContext;
import com.kinetic.TestSupport;
import com.kinetic.exception.AuthException;
import com.kinetic.exception.ValidationException;
import com.kinetic.model.AuthProvider;
import com.kinetic.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthServiceTest {
    private AppContext app;

    @Before
    public void setUp() {
        app = TestSupport.newApp();
    }

    @After
    public void tearDown() {
        app.close();
    }

    @Test
    public void registerThenLogin() throws Exception {
        User u = app.auth.register("Sam Doe", "Sam@Example.com", "Passw0rd1");
        assertNotNull(u.getId());
        assertEquals("sam@example.com", u.getEmail());
        assertEquals(u.getId(), app.auth.login("sam@example.com", "Passw0rd1").getId());
    }

    @Test
    public void duplicateEmailAndWrongPasswordAreRejected() throws Exception {
        app.auth.register("Sam Doe", "sam@example.com", "Passw0rd1");
        assertThrows(ValidationException.class, () -> app.auth.register("Other", "SAM@example.com", "Passw0rd1"));
        assertThrows(AuthException.class, () -> app.auth.login("sam@example.com", "wrong"));
        assertThrows(AuthException.class, () -> app.auth.login("nobody@example.com", "Passw0rd1"));
    }

    @Test
    public void lockoutAfterRepeatedFailures() throws Exception {
        app.auth.register("Sam Doe", "sam@example.com", "Passw0rd1");
        for (int i = 0; i < 5; i++) {
            assertThrows(AuthException.class, () -> app.auth.login("sam@example.com", "bad"));
        }
        AuthException e = assertThrows(AuthException.class, () -> app.auth.login("sam@example.com", "Passw0rd1"));
        assertTrue(e.getMessage().contains("Too many"));
    }

    @Test
    public void googleSignInCreatesThenReusesAccount() throws Exception {
        User first = app.auth.loginWithGoogle("good");
        assertEquals(AuthProvider.GOOGLE, first.getProvider());
        assertEquals("pat@gmail.com", first.getEmail());
        User again = app.auth.loginWithGoogle("good");
        assertEquals(first.getId(), again.getId());
        assertEquals(1, app.users.findAll().size());
        assertThrows(AuthException.class, () -> app.auth.loginWithGoogle("forged"));
    }

    @Test
    public void googleLinksToExistingEmailAccount() throws Exception {
        User local = app.auth.register("Pat Lee", "pat@gmail.com", "Passw0rd1");
        User viaGoogle = app.auth.loginWithGoogle("good");
        assertEquals(local.getId(), viaGoogle.getId());
        User stored = app.users.findById(local.getId()).orElseThrow();
        assertEquals("sub-123", stored.getGoogleSub());
        assertTrue("password must be dropped when Google is linked", !stored.hasPassword());
        assertThrows(AuthException.class, () -> app.auth.login("pat@gmail.com", "Passw0rd1"));
    }

    @Test
    public void googleOnlyAccountCannotUsePasswordLogin() throws Exception {
        app.auth.loginWithGoogle("good");
        AuthException e = assertThrows(AuthException.class, () -> app.auth.login("pat@gmail.com", "anything1"));
        assertTrue(e.getMessage().contains("Google"));
    }
}
