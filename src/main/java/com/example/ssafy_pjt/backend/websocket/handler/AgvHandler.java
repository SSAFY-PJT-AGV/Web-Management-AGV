package com.example.ssafy_pjt.backend.websocket.handler;

import com.example.ssafy_pjt.backend.feature.marker.service.ArucoService;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.websocket.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AgvHandler extends TextWebSocketHandler {

    private final ArucoService arucoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Integer, WebSocketSession> agvSessions = new ConcurrentHashMap<>();
    private final Map<Integer, String> expectedCommandIds = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(5 * 1024 * 1024);
        System.out.println("AGV 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root = objectMapper.readTree(message.getPayload());

        String messageType = root.path("messageType").asText();

        if (!root.has("agvId") || root.path("agvId").isNull()) {
            send(session, new ErrorMessage("ERROR", null, "agvId is required"));
            return;
        }

        Integer agvId = root.path("agvId").asInt();

        agvSessions.put(agvId, session);

        switch (messageType) {
            case "STATUS_REPORT" -> {
                AgvStatusMessage statusMessage =
                        objectMapper.treeToValue(root, AgvStatusMessage.class);
                handleStatusReport(session, statusMessage);
            }
            case "COMMAND_RESULT" -> {
                CommandResultMessage resultMessage =
                        objectMapper.treeToValue(root, CommandResultMessage.class);
                handleCommandResult(session, resultMessage);
            }
            default -> send(session, new ErrorMessage(
                    "ERROR",
                    agvId,
                    "Unsupported messageType: " + messageType
            ));
        }
    }

    private void handleStatusReport(WebSocketSession session, AgvStatusMessage message) throws Exception {
        System.out.println("AGV 상태 수신: " + message.getAgvId());
        System.out.println("status = " + message.getStatus());
        System.out.println("located = " + message.getLocated());
        System.out.println("cargo = " + message.getCargo());
        System.out.println("hasImage = " + message.getHasImage());

        if (Boolean.TRUE.equals(message.getHasImage())
                && message.getImage() != null
                && !message.getImage().isBlank()) {

            VisionResultMessage visionResult =
                    arucoService.detectFromBase64(
                            message.getAgvId(),
                            message.getImage()
                    );

            if (visionResult != null) {
                send(session, visionResult);
                System.out.println("VISION_RESULT 전송: markerId = " + visionResult.getMarkerId());
            } else {
                System.out.println("마커 미검출 → 응답 없음");
            }
        }

        // 테스트용: IDLE이면 첫 command 전송
        /*
        if ("IDLE".equals(message.getStatus())
                && !expectedCommandIds.containsKey(message.getAgvId())) {

            sendCommandAssign(
                    session,
                    message.getAgvId(),
                    "TASK-001",
                    "CMD-001",
                    MissionType.PICK_FROM_STORAGE,
                    101,
                    "CHIP"
            );
        }
         */
    }

    private void handleCommandResult(WebSocketSession session, CommandResultMessage message) throws Exception {
        Integer agvId = message.getAgvId();
        String commandId = message.getCommandId();
        String expectedCommandId = expectedCommandIds.get(agvId);

        if (expectedCommandId == null || !expectedCommandId.equals(commandId)) {
            send(session, new ErrorMessage(
                    "INVALID_COMMAND_RESULT",
                    agvId,
                    "Expected commandId: " + expectedCommandId
            ));
            return;
        }

        if ("FAILED".equals(message.getResult())) {
            expectedCommandIds.remove(agvId);
            send(session, new ErrorMessage(
                    "TASK_ABORTED",
                    agvId,
                    "Command failed: " + message.getErrorCode()
            ));
            return;
        }

        if ("SUCCESS".equals(message.getResult())) {
            if ("CMD-001".equals(commandId)) {
                sendCommandAssign(
                        session,
                        agvId,
                        "TASK-001",
                        "CMD-002",
                        MissionType.DROP_TO_CONVEYOR,
                        201,
                        "CHIP"
                );
            } else if ("CMD-002".equals(commandId)) {
                expectedCommandIds.remove(agvId);

                Map<String, Object> taskDone = Map.of(
                        "messageType", "TASK_DONE",
                        "agvId", agvId,
                        "taskId", "TASK-001"
                );

                send(session, taskDone);
            }
        }
    }

    private void sendCommandAssign(
            WebSocketSession session,
            Integer agvId,
            String taskId,
            String commandId,
            MissionType command,
            Integer destination,
            String cargo
    ) throws Exception {

        expectedCommandIds.put(agvId, commandId);

        CommandAssignMessage response = new CommandAssignMessage(
                "COMMAND_ASSIGN",
                agvId,
                taskId,
                commandId,
                command,
                destination,
                cargo
        );

        send(session, response);
    }

    private void send(WebSocketSession session, Object response) throws Exception {
        session.sendMessage(
                new TextMessage(objectMapper.writeValueAsString(response))
        );
    }
}