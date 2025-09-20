package com.interview.mcp.controller;

import com.interview.mcp.model.*;
import com.interview.mcp.service.McpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*")
public class McpClientController {

    private static final Logger logger = LoggerFactory.getLogger(McpClientController.class);

    @Autowired
    private McpClientService mcpClientService;

    /**
     * Get application health and status
     * 
     * GET /api/mcp/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        List<McpServerInfo> servers = mcpClientService.getConnectedServers();
        int connectedCount = (int) servers.stream().mapToLong(s -> s.isConnected() ? 1 : 0).sum();

        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "totalServers", servers.size(),
            "connectedServers", connectedCount,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Connect to a new MCP server dynamically
     * 
     * POST /api/mcp/connect
     * Body: {
     *   "serverUrl": "http://example.com/mcp",
     *   "serverName": "optional-name"
     * }
     */
    @PostMapping("/connect")
    public CompletableFuture<ResponseEntity<McpConnectionResult>> connectToServer(
            @RequestBody Map<String, String> request) {

        String serverUrl = request.get("serverUrl");
        String serverName = request.get("serverName");

        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            McpConnectionResult errorResult = new McpConnectionResult(false, "Server URL is required", null);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResult));
        }

        logger.info("Received request to connect to MCP server: {}", serverUrl);

        return mcpClientService.connectToMcpServer(serverUrl, serverName)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.status(500).body(result);
                }
            });
    }

    /**
     * Get list of all connected MCP servers
     * 
     * GET /api/mcp/servers
     */
    @GetMapping("/servers")
    public ResponseEntity<List<McpServerInfo>> getConnectedServers() {
        List<McpServerInfo> servers = mcpClientService.getConnectedServers();
        return ResponseEntity.ok(servers);
    }

    /**
     * Disconnect from a specific MCP server
     * 
     * DELETE /api/mcp/servers/{serverName}
     */
    @DeleteMapping("/servers/{serverName}")
    public ResponseEntity<Map<String, Object>> disconnectFromServer(@PathVariable String serverName) {
        boolean success = mcpClientService.disconnectFromServer(serverName);

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "Disconnected successfully" : "Server not found or already disconnected",
            "serverName", serverName
        ));
    }

    /**
     * Get list of all available tools from all connected servers
     * 
     * GET /api/mcp/tools
     */
    @GetMapping("/tools")
    public ResponseEntity<List<McpToolInfo>> getAvailableTools() {
        List<McpToolInfo> tools = mcpClientService.getAvailableTools();
        return ResponseEntity.ok(tools);
    }

    /**
     * Call a specific tool on an MCP server
     * 
     * POST /api/mcp/tools/call
     * Body: {
     *   "serverName": "server-name",
     *   "toolName": "tool-name",
     *   "parameters": { ... }
     * }
     */
    @PostMapping("/tools/call")
    public CompletableFuture<ResponseEntity<McpApiCallResult>> callTool(
            @RequestBody Map<String, Object> request) {

        String serverName = (String) request.get("serverName");
        String toolName = (String) request.get("toolName");
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");

        if (serverName == null || toolName == null) {
            McpApiCallResult errorResult = new McpApiCallResult(false, "serverName and toolName are required", null);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResult));
        }

        logger.info("Received request to call tool {} on server {} with parameters: {}", 
                   toolName, serverName, parameters);

        return mcpClientService.callTool(serverName, toolName, parameters)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.status(500).body(result);
                }
            });
    }

    /**
     * Call any API endpoint on an MCP server
     * 
     * POST /api/mcp/call
     * Body: {
     *   "serverUrl": "http://example.com",
     *   "method": "GET|POST|PUT|DELETE",
     *   "endpoint": "/api/endpoint",
     *   "payload": { ... } // Optional, for POST/PUT requests
     * }
     */
    @PostMapping("/call")
    public CompletableFuture<ResponseEntity<McpApiCallResult>> callApiEndpoint(
            @RequestBody Map<String, Object> request) {

        String serverUrl = (String) request.get("serverUrl");
        String method = (String) request.get("method");
        String endpoint = (String) request.get("endpoint");
        Object payload = request.get("payload");

        if (serverUrl == null || method == null || endpoint == null) {
            McpApiCallResult errorResult = new McpApiCallResult(false, 
                "serverUrl, method, and endpoint are required", null);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResult));
        }

        logger.info("Received request to call {} {} on server {} with payload: {}", 
                   method, endpoint, serverUrl, payload);

        return mcpClientService.callApiEndpoint(serverUrl, method, endpoint, payload)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.status(500).body(result);
                }
            });
    }

    /**
     * Generic endpoint for testing MCP server connectivity
     * 
     * GET /api/mcp/test/{serverName}
     */
    @GetMapping("/test/{serverName}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> testServerConnection(
            @PathVariable String serverName) {

        // Try to list tools to test connectivity
        List<McpToolInfo> tools = mcpClientService.getAvailableTools()
            .stream()
            .filter(tool -> tool.getServerName().equals(serverName))
            .toList();

        boolean connected = !tools.isEmpty();

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
            "serverName", serverName,
            "connected", connected,
            "toolCount", tools.size(),
            "availableTools", tools.stream().map(McpToolInfo::getName).toList(),
            "timestamp", System.currentTimeMillis()
        )));
    }

    /**
     * Get detailed information about a specific tool
     * 
     * GET /api/mcp/tools/{serverName}/{toolName}
     */
    @GetMapping("/tools/{serverName}/{toolName}")
    public ResponseEntity<McpToolInfo> getToolInfo(
            @PathVariable String serverName, 
            @PathVariable String toolName) {

        McpToolInfo tool = mcpClientService.getAvailableTools()
            .stream()
            .filter(t -> t.getServerName().equals(serverName) && t.getName().equals(toolName))
            .findFirst()
            .orElse(null);

        if (tool != null) {
            return ResponseEntity.ok(tool);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}