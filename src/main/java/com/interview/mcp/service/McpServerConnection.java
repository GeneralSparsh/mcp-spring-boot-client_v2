package com.interview.mcp.service;

import com.interview.mcp.schema.McpSchema;
import java.util.List;
import java.util.Map;

/**
 * Interface for MCP server connections
 */
public interface McpServerConnection {

    /**
     * Initialize the connection
     */
    void initialize() throws Exception;

    /**
     * Check if connection is active
     */
    boolean isConnected();

    /**
     * Get server URL
     */
    String getServerUrl();

    /**
     * List available tools
     */
    List<McpSchema.Tool> listTools() throws Exception;

    /**
     * Get available tools as simple list
     */
    List<String> getAvailableTools();

    /**
     * Call a specific tool
     */
    McpSchema.CallToolResult callTool(String toolName, Map<String, Object> parameters) throws Exception;

    /**
     * List available resources
     */
    List<McpSchema.Resource> listResources() throws Exception;

    /**
     * Read a specific resource
     */
    McpSchema.ReadResourceResult readResource(String uri) throws Exception;

    /**
     * Close the connection
     */
    void close() throws Exception;
}
