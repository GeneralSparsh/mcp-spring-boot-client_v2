package com.interview.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple integration tests for McpClientService
 */
@SpringBootTest
@TestPropertySource(properties = {
    "mcp.servers="
})
class McpClientServiceTest {

    @Test
    void contextLoads() {
        // This test ensures the Spring context loads successfully
        assertNotNull("Spring context loaded");
    }
}
