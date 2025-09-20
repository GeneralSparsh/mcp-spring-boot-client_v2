# MCP Spring Boot Client

A comprehensive Model Context Protocol (MCP) client implementation using Spring Boot and Maven.

## Overview

This project demonstrates the ability to ingest MCP server URLs and call any API exposed by those servers using the Model Context Protocol. The implementation is designed to be generic and work with any MCP-compliant server.

## Features

- **Dynamic MCP Server Connection**: Connect to any MCP server by providing its URL
- **Generic API Calling**: Call any tool or API exposed by connected MCP servers
- **Multiple Transport Support**: HTTP and STDIO transport mechanisms
- **RESTful API**: Complete REST API for external integration
- **Comprehensive Testing**: Unit tests covering all major functionality
- **Asynchronous Operations**: Non-blocking operations for better performance
- **Configuration Management**: Flexible configuration for multiple servers
- **Error Handling**: Robust error handling and logging

## Architecture

The project follows a layered architecture:

```
├── Controller Layer (REST API)
├── Service Layer (Business Logic)
├── Model Layer (Data Transfer Objects)
├── Configuration Layer (Application Settings)
└── Transport Layer (MCP Communication)
```

## Project Structure

```
mcp-spring-boot-client/
├── src/
│   ├── main/
│   │   ├── java/com/interview/mcp/
│   │   │   ├── config/
│   │   │   │   └── McpServerProperties.java
│   │   │   ├── controller/
│   │   │   │   └── McpClientController.java
|   |   |   ├──schema/
│   │   │   │   ├── McpSchema.java
│   │   │   ├── model/
│   │   │   │   ├── McpApiCallResult.java
│   │   │   │   ├── McpConnectionResult.java
│   │   │   │   ├── McpServerInfo.java
│   │   │   │   └── McpToolInfo.java
│   │   │   ├── service/
│   │   │   │   ├── HttpMcpServerConnection.java
│   │   │   │   ├── McpClientService.java
│   │   │   │   └── McpServerConnection.java
│   │   │   └── McpClientApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/interview/mcp/
│           ├── McpClientControllerTest.java
│           └── McpClientServiceTest.java
├── pom.xml
└── README.md
```

## Key Components

### 1. McpClientService
The core service that manages MCP server connections and operations:
- Connects to MCP servers using HTTP or STDIO transport
- Discovers available tools and resources
- Executes tool calls and API requests
- Manages multiple server connections concurrently

### 2. McpClientController
REST API controller providing endpoints for:
- `/api/mcp/connect` - Connect to new MCP servers
- `/api/mcp/servers` - List connected servers
- `/api/mcp/tools` - List available tools
- `/api/mcp/tools/call` - Call specific tools
- `/api/mcp/call` - Make generic API calls

### 3. HttpMcpServerConnection
HTTP transport implementation following MCP specification:
- JSON-RPC 2.0 message format
- Tool discovery and execution
- Resource management
- Error handling and logging

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- An MCP-compliant server to connect to

### Build and Run

1. **Clone and build the project:**
   ```bash
   mvn clean package
   ```

2. **Run the application:**
   ```bash
   java -jar target/mcp-spring-boot-client-1.0.0.jar
   ```

3. **Access the application:**
   - Base URL: http://localhost:8080
   - Health check: http://localhost:8080/api/mcp/health

### Configuration

Configure MCP servers in `application.yml`:

```yaml
mcp:
  servers:
    - name: "example-server"
      url: "http://localhost:3000"
      transport: HTTP
```

## API Usage Examples

### 1. Connect to MCP Server
```bash
curl -X POST http://localhost:8080/api/mcp/connect \
  -H "Content-Type: application/json" \
  -d '{
    "serverUrl": "http://localhost:3000",
    "serverName": "my-server"
  }'
```

### 2. List Available Tools
```bash
curl http://localhost:8080/api/mcp/tools
```

### 3. Call a Tool
```bash
curl -X POST http://localhost:8080/api/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "my-server",
    "toolName": "calculator",
    "parameters": {
      "operation": "add",
      "a": 5,
      "b": 3
    }
  }'
```

### 4. Make Generic API Call
```bash
curl -X POST http://localhost:8080/api/mcp/call \
  -H "Content-Type: application/json" \
  -d '{
    "serverUrl": "http://localhost:3000",
    "method": "GET",
    "endpoint": "/api/data"
  }'
```

## Testing

Run unit tests:
```bash
mvn test
```

The test suite includes:
- Service layer tests for MCP operations
- Controller tests for REST API endpoints
- Integration tests for end-to-end scenarios

## Model Context Protocol Compliance

This implementation follows the MCP specification:
- JSON-RPC 2.0 message format
- Standard MCP methods (initialize, tools/list, tools/call, etc.)
- Proper error handling and status codes
- Support for both HTTP and STDIO transports

## Design Principles

- **Generic Implementation**: Works with any MCP-compliant server
- **Scalable Architecture**: Supports multiple concurrent server connections
- **Extensible Design**: Easy to add new transport mechanisms
- **Production Ready**: Comprehensive error handling and logging
- **Testable Code**: High test coverage with unit and integration tests

## Future Enhancements

- WebSocket transport support
- Server-sent events for real-time updates
- Connection pooling and load balancing
- Advanced authentication mechanisms
- Monitoring and metrics integration

## Author



## License

This project is created for interview/demonstration purposes.
