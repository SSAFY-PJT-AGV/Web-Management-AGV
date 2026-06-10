package com.example.ssafy_pjt.backend.websocket.handler;

import com.example.ssafy_pjt.backend.websocket.session.DashboardSessionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class DashboardHandler extends TextWebSocketHandler {

    private final DashboardSessionHandler dashboardSessionHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        dashboardSessionHandler.addSession(session);
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        dashboardSessionHandler.removeSession(session);
    }
}