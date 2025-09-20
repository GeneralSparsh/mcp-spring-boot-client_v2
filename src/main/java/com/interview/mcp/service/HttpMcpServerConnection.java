package com.interview.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.mcp.schema.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

public class HttpMcpServerConnection implements McpServerConnection {

    private static final Logger logger = LoggerFactory.getLogger(HttpMcpServerConnection.class);

    private final String serverName;
    private final String serverUrl;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private boolean connected = false;
    private List<McpSchema.Tool> availableTools = new ArrayList<>();
    private List<McpSchema.Resource> availableResources = new ArrayList<>();

    public HttpMcpServerConnection(String serverName, String serverUrl, WebClient webClient, ObjectMapper objectMapper) {
        this.serverName = serverName;
        this.serverUrl = serverUrl;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void initialize() throws Exception {
        logger.info("Initializing HTTP connection to MCP server: {}", serverUrl);

        try {
            // Send initialization request according to MCP specification
            Map<String, Object> initRequest = createJsonRpcRequest("initialize", Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                    "tools", Map.of("listChanged", true),
                    "resources", Map.of("listChanged", true, "subscribe", true)
                ),
                "clientInfo", Map.of(
                    "name", "MCP Spring Boot Client",
                    "version", "1.0.0"
                )
            ));

            // Make HTTP request to MCP server
            String response = webClient.post()
                .uri(serverUrl + "/mcp")
                .bodyValue(initRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (response != null) {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("result")) {
                    connected = true;
                    logger.info("Successfully initialized connection to MCP server: {}", serverName);

                    // Load available tools and resources
                    loadAvailableTools();
                    loadAvailableResources();
                } else if (responseNode.has("error")) {
                    throw new Exception("MCP server returned error: " + responseNode.get("error"));
                }
            }

        } catch (Exception e) {
            logger.error("Failed to initialize connection to MCP server: {}", serverUrl, e);
            throw e;
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public List<McpSchema.Tool> listTools() throws Exception {
        if (!connected) {
            throw new IllegalStateException("Not connected to MCP server");
        }
        return new ArrayList<>(availableTools);
    }

    @Override
    public List<String> getAvailableTools() {
        return availableTools.stream()
            .map(McpSchema.Tool::name)
            .toList();
    }

    @Override
    public McpSchema.CallToolResult callTool(String toolName, Map<String, Object> parameters) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Not connected to MCP server");
        }

        logger.info("Calling tool {} on server {} with parameters: {}", toolName, serverName, parameters);

        try {
            Map<String, Object> toolRequest = createJsonRpcRequest("tools/call", Map.of(
                "name", toolName,
                "arguments", parameters != null ? parameters : Map.of()
            ));

            String response = webClient.post()
                .uri(serverUrl + "/mcp")
                .bodyValue(toolRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (response != null) {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("result")) {
                    JsonNode result = responseNode.get("result");

                    // Parse the tool result according to MCP specification
                    Object content = parseToolContent(result.get("content"));
                    boolean isError = result.has("isError") && result.get("isError").asBoolean();

                    return new McpSchema.CallToolResult(content, isError);
                } else if (responseNode.has("error")) {
                    JsonNode error = responseNode.get("error");
                    return new McpSchema.CallToolResult(
                        "Error: " + error.get("message").asText(), 
                        true
                    );
                }
            }

            return new McpSchema.CallToolResult("No response from server", true);

        } catch (Exception e) {
            logger.error("Failed to call tool {} on server {}", toolName, serverName, e);
            return new McpSchema.CallToolResult("Tool call failed: " + e.getMessage(), true);
        }
    }

    @Override
    public List<McpSchema.Resource> listResources() throws Exception {
        if (!connected) {
            throw new IllegalStateException("Not connected to MCP server");
        }
        return new ArrayList<>(availableResources);
    }

    @Override
    public McpSchema.ReadResourceResult readResource(String uri) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Not connected to MCP server");
        }

        logger.info("Reading resource {} from server {}", uri, serverName);

        try {
            Map<String, Object> resourceRequest = createJsonRpcRequest("resources/read", Map.of(
                "uri", uri
            ));

            String response = webClient.post()
                .uri(serverUrl + "/mcp")
                .bodyValue(resourceRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (response != null) {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("result")) {
                    JsonNode result = responseNode.get("result");
                    JsonNode contents = result.get("contents");

                    List<Object> contentList = new ArrayList<>();
                    if (contents != null && contents.isArray()) {
                        for (JsonNode content : contents) {
                            contentList.add(parseResourceContent(content));
                        }
                    }

                    return new McpSchema.ReadResourceResult(contentList);
                } else if (responseNode.has("error")) {
                    throw new Exception("Error reading resource: " + responseNode.get("error").get("message"));
                }
            }

            throw new Exception("No response from server");

        } catch (Exception e) {
            logger.error("Failed to read resource {} from server {}", uri, serverName, e);
            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        if (connected) {
            logger.info("Closing connection to MCP server: {}", serverName);
            connected = false;
            availableTools.clear();
            availableResources.clear();
        }
    }

    /**
     * Load available tools from the MCP server
     */
    private void loadAvailableTools() {
        try {
            Map<String, Object> toolsRequest = createJsonRpcRequest("tools/list", Map.of());

            String response = webClient.post()
                .uri(serverUrl + "/mcp")
                .bodyValue(toolsRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (response != null) {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("result") && responseNode.get("result").has("tools")) {
                    JsonNode tools = responseNode.get("result").get("tools");

                    availableTools.clear();
                    for (JsonNode tool : tools) {
                        String name = tool.get("name").asText();
                        String description = tool.has("description") ? tool.get("description").asText() : "";
                        JsonNode inputSchema = tool.has("inputSchema") ? tool.get("inputSchema") : null;

                        availableTools.add(new McpSchema.Tool(name, description, inputSchema));
                    }

                    logger.info("Loaded {} tools from MCP server: {}", availableTools.size(), serverName);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load tools from MCP server: {}", serverName, e);
        }
    }

    /**
     * Load available resources from the MCP server
     */
    private void loadAvailableResources() {
        try {
            Map<String, Object> resourcesRequest = createJsonRpcRequest("resources/list", Map.of());

            String response = webClient.post()
                .uri(serverUrl + "/mcp")
                .bodyValue(resourcesRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (response != null) {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("result") && responseNode.get("result").has("resources")) {
                    JsonNode resources = responseNode.get("result").get("resources");

                    availableResources.clear();
                    for (JsonNode resource : resources) {
                        String uri = resource.get("uri").asText();
                        String name = resource.has("name") ? resource.get("name").asText() : "";
                        String description = resource.has("description") ? resource.get("description").asText() : "";
                        String mimeType = resource.has("mimeType") ? resource.get("mimeType").asText() : null;

                        availableResources.add(new McpSchema.Resource(uri, name, description, mimeType, null));
                    }

                    logger.info("Loaded {} resources from MCP server: {}", availableResources.size(), serverName);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load resources from MCP server: {}", serverName, e);
        }
    }

    /**
     * Create JSON-RPC request according to MCP specification
     */
    private Map<String, Object> createJsonRpcRequest(String method, Map<String, Object> params) {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", UUID.randomUUID().toString());
        request.put("method", method);
        if (params != null && !params.isEmpty()) {
            request.put("params", params);
        }
        return request;
    }

    /**
     * Parse tool content from MCP response
     */
    private Object parseToolContent(JsonNode content) {
        if (content == null) {
            return null;
        }

        if (content.isArray()) {
            List<Object> contentList = new ArrayList<>();
            for (JsonNode item : content) {
                if (item.has("type") && item.has("text")) {
                    contentList.add(Map.of(
                        "type", item.get("type").asText(),
                        "text", item.get("text").asText()
                    ));
                } else {
                    contentList.add(item.toString());
                }
            }
            return contentList;
        } else if (content.has("text")) {
            return content.get("text").asText();
        } else {
            return content.toString();
        }
    }

    /**
     * Parse resource content from MCP response
     */
    private Object parseResourceContent(JsonNode content) {
        if (content.has("type") && content.has("text")) {
            return Map.of(
                "type", content.get("type").asText(),
                "text", content.get("text").asText()
            );
        } else if (content.has("type") && content.has("blob")) {
            return Map.of(
                "type", content.get("type").asText(),
                "blob", content.get("blob").asText()
            );
        } else {
            return content.toString();
        }
    }
}
