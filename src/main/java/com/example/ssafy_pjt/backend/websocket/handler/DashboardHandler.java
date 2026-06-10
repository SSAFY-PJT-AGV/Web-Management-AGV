package com.example.ssafy_pjt.backend.websocket.handler;

import com.example.ssafy_pjt.backend.websocket.session.DashboardSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Component
@RequiredArgsConstructor
public class DashboardHandler
        extends TextWebSocketHandler {


    private final DashboardSessionManager manager;


    @Override
    public void afterConnectionEstablished(
            WebSocketSession session
    ) {
        manager.add(session);
    }


    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        manager.remove(session);
    }
}