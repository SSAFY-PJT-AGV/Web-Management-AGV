package com.example.ssafy_pjt.backend.websocket.sender;

import com.example.ssafy_pjt.backend.websocket.session.DashboardSessionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DashboardSender {

    private final DashboardSessionHandler dashboardSessionHandler;
    private final ObjectMapper objectMapper;

    public void send(String type, Object data) {
        Map<String, Object> message = Map.of(
                "type", type,
                "data", data
        );

        sendMessage(message);
    }

    private void sendMessage(Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);

            for (WebSocketSession session
                    : dashboardSessionHandler.getSessions()) {

                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Dashboard WebSocket 전송 실패",
                    e
            );
        }
    }
}