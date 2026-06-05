package com.example.ssafy_pjt.backend.websocket.sender;

import com.example.ssafy_pjt.backend.websocket.dto.CommandAssignMessage;
import com.example.ssafy_pjt.backend.websocket.dto.VisionResultMessage;
import com.example.ssafy_pjt.backend.websocket.session.AgvSessionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class AgvCommandSender {

    private final AgvSessionHandler agvSessionHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendCommand(Integer agvId, CommandAssignMessage message) {
        send(agvId, message, "COMMAND_ASSIGN");
    }

    public void sendVisionResult(Integer agvId, VisionResultMessage message) {
        send(agvId, message, "VISION_RESULT");
    }

    private void send(Integer agvId, Object message, String logType) {
        try {
            WebSocketSession session = agvSessionHandler.getSession(agvId);

            if (session == null || !session.isOpen()) {
                throw new IllegalStateException("AGV 연결 없음. agvId=" + agvId);
            }

            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));

            System.out.println("[" + logType + " SEND] agvId=" + agvId + ", message=" + json);

        } catch (Exception e) {
            throw new RuntimeException(logType + " 전송 실패", e);
        }
    }
}