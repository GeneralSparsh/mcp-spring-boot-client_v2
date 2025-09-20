package com.interview.mcp;

import com.interview.mcp.controller.McpClientController;
import com.interview.mcp.service.McpClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Simple controller tests
 */
@WebMvcTest(McpClientController.class)
class McpClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private McpClientService mcpClientService;

    @Test
    void testHealthEndpoint() throws Exception {
        // Given
        when(mcpClientService.getConnectedServers()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/mcp/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void testToolsEndpoint() throws Exception {
        // Given
        when(mcpClientService.getAvailableTools()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
