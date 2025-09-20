package com.interview.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Information about an MCP tool
 */
public class McpToolInfo {
    private String name;
    private String description;
    private String serverName;
    private String serverUrl;
    private JsonNode inputSchema;

    public McpToolInfo() {}

    public McpToolInfo(String name, String description, String serverName, String serverUrl, JsonNode inputSchema) {
        this.name = name;
        this.description = description;
        this.serverName = serverName;
        this.serverUrl = serverUrl;
        this.inputSchema = inputSchema;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

    public JsonNode getInputSchema() { return inputSchema; }
    public void setInputSchema(JsonNode inputSchema) { this.inputSchema = inputSchema; }
}