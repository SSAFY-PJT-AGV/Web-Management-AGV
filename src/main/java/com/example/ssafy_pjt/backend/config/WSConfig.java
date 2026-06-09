package com.example.ssafy_pjt.backend.config;

import com.example.ssafy_pjt.backend.websocket.handler.AgvHandler;
import com.example.ssafy_pjt.backend.websocket.handler.DashboardHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WSConfig implements WebSocketConfigurer {

    private final AgvHandler agvHandler;
    private final DashboardHandler dashboardHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(agvHandler, "/ws/agv")
                .setAllowedOrigins("*");

        registry.addHandler(dashboardHandler, "/ws/dashboard")
                .setAllowedOrigins("*");
    }
}