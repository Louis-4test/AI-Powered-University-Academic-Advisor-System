package com.pecar.academic.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", 
            "3a8f2b1c9d4e7f0a5b6c3d2e1f8a9b0c3a8f2b1c9d4e7f0a5b6c3d2e1f8a9b0c");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000L);
    }

    @Test
    void generateAndValidateToken() {
        String email = "test@pecar.edu";
        String token = jwtUtils.generateToken(email);
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(email, jwtUtils.extractEmail(token));
    }

    @Test
    void invalidateExpired() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1L);
        String token = jwtUtils.generateToken("expired@pecar.edu");
        assertFalse(jwtUtils.validateToken(token));
    }
}
