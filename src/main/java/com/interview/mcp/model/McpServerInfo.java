package com.interview.mcp.model;

/**
 * Information about a connected MCP server
 */
public class McpServerInfo {
    private String name;
    private String url;
    private boolean connected;
    private int toolCount;

    public McpServerInfo() {}

    public McpServerInfo(String name, String url, boolean connected, int toolCount) {
        this.name = name;
        this.url = url;
        this.connected = connected;
        this.toolCount = toolCount;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public int getToolCount() { return toolCount; }
    public void setToolCount(int toolCount) { this.toolCount = toolCount; }
}