package com.example.ssafy_pjt.backend.websocket.handler;

import com.example.ssafy_pjt.backend.feature.marker.service.ArucoService;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.websocket.dto.*;
import com.example.ssafy_pjt.backend.websocket.session.AgvSessionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AgvHandler extends TextWebSocketHandler {

    private final ArucoService arucoService;
    private final AgvSessionHandler agvSessionHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Integer, Long> expectedCommandIds = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(5 * 1024 * 1024);
        System.out.println("AGV 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    ) throws Exception {

        JsonNode root = objectMapper.readTree(message.getPayload());

        Integer agvId = root.path("agvId").asInt(0);

        if (agvId == 0) {
            send(session, new ErrorMessage("ERROR", null, "agvId is required"));
            return;
        }

        agvSessionHandler.addSession(agvId, session);

        AgvStatusMessage statusMessage =
                objectMapper.treeToValue(root, AgvStatusMessage.class);

        handleStatusReport(session, statusMessage);
    }

    private void handleStatusReport(
            WebSocketSession session,
            AgvStatusMessage message
    ) throws Exception {

        System.out.println("AGV 상태 수신: " + message.getAgvId());
        System.out.println("status = " + message.getStatus());
        System.out.println("located = " + message.getLocated());
        System.out.println("cargo = " + message.getCargo());

        if (Boolean.TRUE.equals(message.getHasImage())
                && message.getImage() != null
                && !message.getImage().isBlank()) {

            ArucoResultMessage arucoResult =
                    arucoService.detectFromBase64(
                            message.getAgvId(),
                            message.getImage()
                    );

            if (arucoResult != null) {
                send(session, arucoResult);
            } else {
                System.out.println("마커 인식 실패: agvId=" + message.getAgvId());
            }
        }
    }

    private void handleCommandResult(
            WebSocketSession session,
            CommandResultMessage message
    ) {

        System.out.println("COMMAND_RESULT 수신");
        System.out.println("AGV = " + message.getAgvId());
        System.out.println("commandId = " + message.getCommandId());
        System.out.println("result = " + message.getResult());

        if ("SUCCESS".equals(message.getResult())) {

            System.out.println(
                    "Mission 성공 처리: commandId="
                            + message.getCommandId()
            );

        } else if ("FAILED".equals(message.getResult())) {

            System.out.println(
                    "Mission 실패: "
                            + message.getErrorCode()
            );
        }
    }

    private void sendCommandAssign(
            WebSocketSession session,
            Integer agvId,
            Long taskId,
            Long commandId,
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
        if (response == null) {
            return;
        }

        session.sendMessage(
                new TextMessage(objectMapper.writeValueAsString(response))
        );
    }
}