package com.interview.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@ConfigurationProperties(prefix = "mcp")
public class McpServerProperties {

    private List<ServerConfig> servers = new ArrayList<>();

    public List<ServerConfig> getServers() {
        return servers;
    }

    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }


    public static class ServerConfig {
        private String name;
        private String url;
        private TransportType transport = TransportType.HTTP;
        private String command; // For STDIO transport
        private List<String> args = new ArrayList<>(); // For STDIO transport

        // Constructors
        public ServerConfig() {}

        public ServerConfig(String name, String url, TransportType transport) {
            this.name = name;
            this.url = url;
            this.transport = transport;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public TransportType getTransport() { return transport; }
        public void setTransport(TransportType transport) { this.transport = transport; }

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }

        public List<String> getArgs() { return args; }
        public void setArgs(List<String> args) { this.args = args; }
    }

    
    public enum TransportType {
        HTTP, STDIO
    }
}