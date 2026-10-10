package com.kinetic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.kinetic.util.PasswordHasher;
import org.junit.Test;

public class PasswordHasherTest {
    @Test
    public void hashVerifiesAndIsSalted() {
        String a = PasswordHasher.hash("Secret123");
        String b = PasswordHasher.hash("Secret123");
        assertNotEquals("same password must hash differently (random salt)", a, b);
        assertTrue(PasswordHasher.verify("Secret123", a));
        assertFalse(PasswordHasher.verify("secret123", a));
    }

    @Test
    public void malformedHashIsRejectedNotThrown() {
        assertFalse(PasswordHasher.verify("x", "garbage"));
        assertFalse(PasswordHasher.verify("x", null));
    }
}
