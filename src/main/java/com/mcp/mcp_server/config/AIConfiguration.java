package com.mcp.mcp_server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring AI Configuration for Claude Haiku integration
 */
@Configuration
public class AIConfiguration {

    /**
     * Configure ChatClient for Claude Haiku
     * Spring AI will auto-configure this based on spring.ai.anthropic properties
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * ObjectMapper for JSON processing in AI services
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * RestTemplate for HTTP calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

