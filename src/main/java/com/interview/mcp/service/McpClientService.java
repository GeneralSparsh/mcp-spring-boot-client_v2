package com.interview.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.mcp.config.McpServerProperties;
import com.interview.mcp.model.*;
import com.interview.mcp.schema.McpSchema;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class McpClientService {

    private static final Logger logger = LoggerFactory.getLogger(McpClientService.class);

    @Autowired
    private McpServerProperties mcpServerProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, McpServerConnection> activeConnections = new ConcurrentHashMap<>();
    private final WebClient webClient;

    public McpClientService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    /**
     * Initialize connections to configured MCP servers
     */
    @PostConstruct
    public void initialize() {
        logger.info("Initializing MCP Client Service with {} configured servers", 
                   mcpServerProperties.getServers().size());

        for (McpServerProperties.ServerConfig serverConfig : mcpServerProperties.getServers()) {
            try {
                connectToServer(serverConfig);
            } catch (Exception e) {
                logger.error("Failed to connect to MCP server: {}", serverConfig.getName(), e);
            }
        }
    }

    /**
     * Connect to a new MCP server dynamically
     * 
     * @param serverUrl The URL of the MCP server
     * @param serverName Optional name for the server
     * @return Connection result
     */
    public CompletableFuture<McpConnectionResult> connectToMcpServer(String serverUrl, String serverName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String name = serverName != null ? serverName : "server-" + System.currentTimeMillis();
                McpServerProperties.ServerConfig config = new McpServerProperties.ServerConfig(
                    name, serverUrl, McpServerProperties.TransportType.HTTP);

                McpServerConnection connection = connectToServer(config);

// Convert List<String> to List<McpToolInfo> for the connection result
List<String> toolNames = connection.getAvailableTools();
List<McpToolInfo> toolInfos = new ArrayList<>();
for (String toolName : toolNames) {
    toolInfos.add(new McpToolInfo(toolName, "", name, config.getUrl(), null));
}

return new McpConnectionResult(true, "Successfully connected to " + name, toolInfos);

            } catch (Exception e) {
                logger.error("Failed to connect to MCP server: {}", serverUrl, e);
                return new McpConnectionResult(false, "Connection failed: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Get list of all available tools from all connected servers
     */
    public List<McpToolInfo> getAvailableTools() {
        List<McpToolInfo> allTools = new ArrayList<>();

        for (Map.Entry<String, McpServerConnection> entry : activeConnections.entrySet()) {
            String serverName = entry.getKey();
            McpServerConnection connection = entry.getValue();

            try {
                List<McpSchema.Tool> tools = connection.listTools();
                for (McpSchema.Tool tool : tools) {
                    allTools.add(new McpToolInfo(
                        tool.name(),
                        tool.description(),
                        serverName,
                        connection.getServerUrl(),
                        tool.inputSchema()
                    ));
                }
            } catch (Exception e) {
                logger.error("Failed to list tools from server: {}", serverName, e);
            }
        }

        return allTools;
    }

    /**
     * Call a specific tool on an MCP server
     * 
     * @param serverName Name of the server
     * @param toolName Name of the tool to call
     * @param parameters Parameters for the tool
     * @return Result of the tool execution
     */
    public CompletableFuture<McpApiCallResult> callTool(String serverName, String toolName, 
                                                       Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                McpServerConnection connection = activeConnections.get(serverName);
                if (connection == null) {
                    return new McpApiCallResult(false, "Server not found: " + serverName, null);
                }

                McpSchema.CallToolResult result = connection.callTool(toolName, parameters);

                return new McpApiCallResult(
                    !result.isError(),
                    result.isError() ? "Tool execution failed" : "Success",
                    result.content()
                );

            } catch (Exception e) {
                logger.error("Failed to call tool {} on server {}", toolName, serverName, e);
                return new McpApiCallResult(false, "Tool call failed: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Call any API endpoint on an MCP server by URL
     * 
     * @param serverUrl Base URL of the MCP server
     * @param method HTTP method (GET, POST, etc.)
     * @param endpoint Specific endpoint path
     * @param payload Request payload (for POST/PUT requests)
     * @return API call result
     */
    public CompletableFuture<McpApiCallResult> callApiEndpoint(String serverUrl, String method, 
                                                              String endpoint, Object payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fullUrl = serverUrl.endsWith("/") ? serverUrl + endpoint : serverUrl + "/" + endpoint;

                Mono<String> responseMono;

                switch (method.toUpperCase()) {
                    case "GET":
                        responseMono = webClient.get()
                            .uri(fullUrl)
                            .retrieve()
                            .bodyToMono(String.class);
                        break;
                    case "POST":
                        responseMono = webClient.post()
                            .uri(fullUrl)
                            .bodyValue(payload != null ? payload : "")
                            .retrieve()
                            .bodyToMono(String.class);
                        break;
                    case "PUT":
                        responseMono = webClient.put()
                            .uri(fullUrl)
                            .bodyValue(payload != null ? payload : "")
                            .retrieve()
                            .bodyToMono(String.class);
                        break;
                    case "DELETE":
                        responseMono = webClient.delete()
                            .uri(fullUrl)
                            .retrieve()
                            .bodyToMono(String.class);
                        break;
                    default:
                        return new McpApiCallResult(false, "Unsupported HTTP method: " + method, null);
                }

                String response = responseMono.block();
                return new McpApiCallResult(true, "API call successful", response);

            } catch (Exception e) {
                logger.error("Failed to call API endpoint {} {} on server {}", method, endpoint, serverUrl, e);
                return new McpApiCallResult(false, "API call failed: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Get list of connected servers
     */
    public List<McpServerInfo> getConnectedServers() {
        return activeConnections.entrySet().stream()
            .map(entry -> new McpServerInfo(
                entry.getKey(),
                entry.getValue().getServerUrl(),
                entry.getValue().isConnected(),
                entry.getValue().getAvailableTools().size()
            ))
            .toList();
    }

    /**
     * Disconnect from a specific server
     */
    public boolean disconnectFromServer(String serverName) {
        McpServerConnection connection = activeConnections.remove(serverName);
        if (connection != null) {
            try {
                connection.close();
                logger.info("Disconnected from MCP server: {}", serverName);
                return true;
            } catch (Exception e) {
                logger.error("Error disconnecting from server: {}", serverName, e);
            }
        }
        return false;
    }

    /**
     * Create connection to MCP server
     */
    private McpServerConnection connectToServer(McpServerProperties.ServerConfig config) throws Exception {
        logger.info("Connecting to MCP server: {} at {}", config.getName(), config.getUrl());

        McpServerConnection connection;

        if (config.getTransport() == McpServerProperties.TransportType.STDIO) {
            // STDIO transport for local MCP servers
            connection = createStdioConnection(config);
        } else {
            // HTTP transport for remote MCP servers
            connection = createHttpConnection(config);
        }

        // Test the connection
        connection.initialize();

        activeConnections.put(config.getName(), connection);
        logger.info("Successfully connected to MCP server: {}", config.getName());

        return connection;
    }

    private McpServerConnection createStdioConnection(McpServerProperties.ServerConfig config) throws Exception {
        return new HttpMcpServerConnection(config.getName(), config.getUrl(), webClient, objectMapper);
    }

    private McpServerConnection createHttpConnection(McpServerProperties.ServerConfig config) throws Exception {
        return new HttpMcpServerConnection(config.getName(), config.getUrl(), webClient, objectMapper);
    }

    /**
     * Cleanup connections on shutdown
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up MCP connections...");

        for (Map.Entry<String, McpServerConnection> entry : activeConnections.entrySet()) {
            try {
                entry.getValue().close();
                logger.info("Closed connection to: {}", entry.getKey());
            } catch (Exception e) {
                logger.error("Error closing connection to: {}", entry.getKey(), e);
            }
        }

        activeConnections.clear();
    }
}
