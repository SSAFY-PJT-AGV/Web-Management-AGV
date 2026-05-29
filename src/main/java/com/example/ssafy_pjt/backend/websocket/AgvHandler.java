package com.example.ssafy_pjt.backend.websocket;

import com.example.ssafy_pjt.backend.feature.marker.ArucoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AgvHandler extends TextWebSocketHandler {

    private final ArucoService arucoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String expectedCommandId = null;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        session.setTextMessageSizeLimit(1024 * 1024);

        System.out.println(
                "메시지 제한: "
                        + session.getTextMessageSizeLimit()
        );

        System.out.println("AGV 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("수신 메시지: " + payload);

        JsonNode root = objectMapper.readTree(payload);

        String type = root.path("type").asText();
        String agvId = root.path("agvId").asText();

        if ("STATUS".equals(type)) {
            handleStatus(session, root, agvId);
            return;
        }

        if ("COMMAND_RESULT".equals(type)) {
            handleCommandResult(session, root, agvId);
        }
    }

    private void handleStatus(WebSocketSession session, JsonNode root, String agvId) throws Exception {
        String status = root.path("status").asText();
        boolean hasImage = root.path("hasImage").asBoolean(false);

        if (hasImage) {
            String imageBase64 = root.path("imageBase64").asText();

            Map<String, Object> markerResult = arucoService.detectFromBase64(agvId, imageBase64);

            session.sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(markerResult))
            );
        }

        if ("IDLE".equals(status) && expectedCommandId == null) {
            sendPickFromStorage(session, agvId);
            expectedCommandId = "CMD-001";
        }
    }

    private void handleCommandResult(WebSocketSession session, JsonNode root, String agvId) throws Exception {
        String commandId = root.path("commandId").asText();
        String result = root.path("result").asText();

        if (!commandId.equals(expectedCommandId)) {
            sendInvalidCommandResult(session, agvId);
            return;
        }

        if ("FAILED".equals(result)) {
            sendTaskAborted(session, agvId, commandId, root.path("errorCode").asText("COMMAND_FAILED"));
            expectedCommandId = null;
            return;
        }

        if ("SUCCESS".equals(result)) {
            if ("CMD-001".equals(expectedCommandId)) {
                sendDeliverToAssembly(session, agvId);
                expectedCommandId = "CMD-002";
            } else if ("CMD-002".equals(expectedCommandId)) {
                sendTaskDone(session, agvId);
                expectedCommandId = null;
            }
        }
    }

    private void sendPickFromStorage(WebSocketSession session, String agvId) throws Exception {
        String response = """
        {
          "type":"COMMAND_ASSIGN",
          "agvId":"%s",
          "taskId":"TASK-001",
          "commandId":"CMD-001",
          "command":"PICK_FROM_STORAGE",
          "materialCode":"A"
        }
        """.formatted(agvId);

        session.sendMessage(new TextMessage(response));
    }

    private void sendDeliverToAssembly(WebSocketSession session, String agvId) throws Exception {
        String response = """
        {
          "type":"COMMAND_ASSIGN",
          "agvId":"%s",
          "taskId":"TASK-001",
          "commandId":"CMD-002",
          "command":"DELIVER_TO_ASSEMBLY"
        }
        """.formatted(agvId);

        session.sendMessage(new TextMessage(response));
    }

    private void sendTaskDone(WebSocketSession session, String agvId) throws Exception {
        String response = """
        {
          "type":"TASK_DONE",
          "agvId":"%s",
          "taskId":"TASK-001"
        }
        """.formatted(agvId);

        session.sendMessage(new TextMessage(response));
    }

    private void sendTaskAborted(WebSocketSession session, String agvId, String failedCommandId, String reason) throws Exception {
        String response = """
        {
          "type":"TASK_ABORTED",
          "agvId":"%s",
          "taskId":"TASK-001",
          "failedCommandId":"%s",
          "reason":"%s"
        }
        """.formatted(agvId, failedCommandId, reason);

        session.sendMessage(new TextMessage(response));
    }

    private void sendInvalidCommandResult(WebSocketSession session, String agvId) throws Exception {
        String response = """
        {
          "type":"INVALID_COMMAND_RESULT",
          "agvId":"%s",
          "taskId":"TASK-001",
          "expectedCommandId":"%s",
          "message":"Unexpected command result received"
        }
        """.formatted(agvId, expectedCommandId);

        session.sendMessage(new TextMessage(response));
    }
}