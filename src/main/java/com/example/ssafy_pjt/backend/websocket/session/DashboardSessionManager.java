package com.example.ssafy_pjt.backend.websocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DashboardSessionManager {

    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();


    public void add(WebSocketSession session) {
        sessions.add(session);
        log.info("Dashboard 연결 {}", session.getId());
    }


    public void remove(WebSocketSession session) {
        sessions.remove(session);
        log.info("Dashboard 종료 {}", session.getId());
    }


    public Set<WebSocketSession> getSessions() {
        return sessions;
    }
}