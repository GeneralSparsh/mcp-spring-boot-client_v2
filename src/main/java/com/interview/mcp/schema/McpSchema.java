package com.interview.mcp.schema;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;


public class McpSchema {

    /**
     * Represents an MCP tool with name, description, and input schema
     */
    public record Tool(String name, String description, JsonNode inputSchema) {
        
        public Tool(String name, String description, JsonNode inputSchema) {
            this.name = name != null ? name : "";
            this.description = description != null ? description : "";
            this.inputSchema = inputSchema;
        }
    }

    /**
     * Represents the result of calling an MCP tool
     */
    public record CallToolResult(Object content, boolean isError) {
        
        public CallToolResult(Object content, boolean isError) {
            this.content = content;
            this.isError = isError;
        }
        
        // Convenience constructor for error results
        public CallToolResult(String errorMessage) {
            this(errorMessage, true);
        }
    }

    /**
     * Represents an MCP resource with URI, name, description, and metadata
     */
    public record Resource(String uri, String name, String description, String mimeType, Object annotations) {
        
        public Resource(String uri, String name, String description, String mimeType, Object annotations) {
            this.uri = uri != null ? uri : "";
            this.name = name != null ? name : "";
            this.description = description != null ? description : "";
            this.mimeType = mimeType;
            this.annotations = annotations;
        }
    }

    /**
     * Represents the result of reading an MCP resource
     */
    public record ReadResourceResult(List<Object> contents) {
        
        public ReadResourceResult(List<Object> contents) {
            this.contents = contents != null ? contents : List.of();
        }
    }
}
