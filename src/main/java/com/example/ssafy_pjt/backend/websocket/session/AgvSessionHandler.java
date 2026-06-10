package com.example.ssafy_pjt.backend.websocket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgvSessionHandler {

    private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(Integer agvId, WebSocketSession session) {
        sessions.put(agvId, session);
    }

    public WebSocketSession getSession(Integer agvId) {
        return sessions.get(agvId);
    }

    public void removeSession(WebSocketSession session) {
        sessions.entrySet().removeIf(entry ->
                entry.getValue().getId().equals(session.getId())
        );
    }

    public boolean isConnected(Integer agvId) {
        WebSocketSession session = sessions.get(agvId);
        return session != null && session.isOpen();
    }

    public Integer findAgvIdBySession(WebSocketSession session) {
        return sessions.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue().getId().equals(session.getId())
                )
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}