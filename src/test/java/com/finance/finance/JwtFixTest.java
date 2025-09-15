package com.finance.finance;

import com.finance.finance.config.JwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=FinanceAppSecretKey2024!@#$%^&*()FinanceAppSecretKey2024!@#$%^&*()FinanceAppSecretKey2024!@#$%^&*()FinanceAppSecretKey2024!@#$%^&*()",
    "jwt.expiration=3600000"
})
class JwtFixTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void testJwtTokenGeneration_ShouldNotThrowException() {
        // This test verifies that the JWT configuration works without the HS512 key size error
        assertDoesNotThrow(() -> {
            String token = jwtConfig.generateToken("testuser", "USER");
            assertNotNull(token);
            assertTrue(token.length() > 0);
        });
    }

    @Test
    void testJwtTokenValidation_ShouldWork() {
        // This test verifies that token validation works
        assertDoesNotThrow(() -> {
            String token = jwtConfig.generateToken("testuser", "USER");
            boolean isValid = jwtConfig.validateToken(token);
            assertTrue(isValid);
        });
    }
}
