package com.balgoorm.balgoorm_backend.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/api/sub");
        registry.setApplicationDestinationPrefixes("/api/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/api/chat")
                .setAllowedOriginPatterns("*");
    }
}
