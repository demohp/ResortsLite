package com.demo.resortslite;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ResortsLiteApplicationTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Spring context load test
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts without errors
        // This is the most fundamental integration test for a Spring Boot app
    }

    // ─────────────────────────────────────────────────────────────────────────
    // main() method test
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void main_doesNotThrowException() {
        // Arrange & Act & Assert
        // Calling main() should not throw any exception
        // Spring Boot will start and immediately return in test context
        assertDoesNotThrow(() ->
                ResortsLiteApplication.main(new String[]{}));
    }

    @Test
    void main_withArgs_doesNotThrowException() {
        // Arrange
        String[] args = {"--spring.main.web-application-type=none"};

        // Act & Assert
        assertDoesNotThrow(() ->
                ResortsLiteApplication.main(args));
    }
}
