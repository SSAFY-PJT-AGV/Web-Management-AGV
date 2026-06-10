package com.example.ssafy_pjt.backend.websocket.sender;

import com.example.ssafy_pjt.backend.websocket.session.DashboardSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class DashboardSender {

    private final DashboardSessionManager dashboardSessionManager;
    private final ObjectMapper objectMapper;

    public void broadcast(Object payload) {
        for (WebSocketSession session : dashboardSessionManager.getSessions()) {
            try {
                if (session.isOpen()) {
                    String json = objectMapper.writeValueAsString(payload);
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                System.out.println("[DASHBOARD BROADCAST ERROR] " + e.getMessage());
            }
        }
    }
}