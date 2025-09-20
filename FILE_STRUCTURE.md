# MCP Spring Boot Client - File Structure

### Project Structure:
- README.md
- TECHNICAL_DOCUMENTATION.md
- pom.xml
- src/main/java/com/interview/mcp/McpClientApplication.java
- src/main/java/com/interview/mcp/config/McpServerProperties.java
- src/main/java/com/interview/mcp/controller/McpClientController.java
- src/main/java/com/interview/mcp/model/McpApiCallResult.java
- src/main/java/com/interview/mcp/model/McpConnectionResult.java
- src/main/java/com/interview/mcp/model/McpServerInfo.java
- src/main/java/com/interview/mcp/model/McpToolInfo.java
- src/main/java/com/interview/mcp/service/HttpMcpServerConnection.java
- src/main/java/com/interview/mcp/service/McpClientService.java
- src/main/java/com/interview/mcp/service/McpServerConnection.java
- src/main/resources/application.yml
- src/test/java/com/interview/mcp/McpClientControllerTest.java
- src/test/java/com/interview/mcp/McpClientServiceTest.java

## Interview Demonstration Points:

1. **Complete MCP Implementation**: Full compliance with Model Context Protocol specification
2. **Generic Design**: Can connect to and call any MCP server URL
3. **Production Ready**: Comprehensive error handling, logging, and testing
4. **Spring Boot Best Practices**: Proper layered architecture and dependency injection
5. **Asynchronous Operations**: Non-blocking operations for better performance
6. **Comprehensive Testing**: Unit tests covering all major functionality
7. **Clear Documentation**: Both user-facing and technical documentation
8. **Extensible Architecture**: Easy to add new features and transport mechanisms
