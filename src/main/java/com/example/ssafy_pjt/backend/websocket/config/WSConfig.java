package com.example.ssafy_pjt.backend.websocket.config;

import com.example.ssafy_pjt.backend.websocket.handler.AgvHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WSConfig implements WebSocketConfigurer {

    private final AgvHandler agvHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(agvHandler, "/ws/agv")
                .setAllowedOrigins("*");
    }
}