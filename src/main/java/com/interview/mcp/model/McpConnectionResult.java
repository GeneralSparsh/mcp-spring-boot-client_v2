package com.interview.mcp.model;

import java.util.List;

/**
 * Result of MCP server connection attempt
 */
public class McpConnectionResult {
    private boolean success;
    private String message;
    private List<McpToolInfo> availableTools;

    public McpConnectionResult() {}

    public McpConnectionResult(boolean success, String message, List<McpToolInfo> availableTools) {
        this.success = success;
        this.message = message;
        this.availableTools = availableTools;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<McpToolInfo> getAvailableTools() { return availableTools; }
    public void setAvailableTools(List<McpToolInfo> availableTools) { this.availableTools = availableTools; }
}