package com.example.ssafy_pjt.backend.websocket.handler;

import com.example.ssafy_pjt.backend.feature.agv.service.AgvService;
import com.example.ssafy_pjt.backend.feature.agv.service.ChipScenarioTestService;
import com.example.ssafy_pjt.backend.feature.marker.service.ArucoService;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionResultService;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.dto.ArucoResultMessage;
import com.example.ssafy_pjt.backend.websocket.dto.ErrorMessage;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardSender;
import com.example.ssafy_pjt.backend.websocket.session.AgvSessionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AgvHandler extends TextWebSocketHandler {

    private final ArucoService arucoService;
    private final AgvSessionHandler agvSessionHandler;
    private final AgvService agvService;
    private final ChipScenarioTestService chipScenarioTestService;
    private final MissionResultService missionResultService;
    private final ObjectMapper objectMapper;
    private final DashboardSender dashboardSender;

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
    ) {
        try {
            System.out.println("\n========== [WS RAW RECEIVE] ==========");
            System.out.println("sessionId = " + session.getId());
            System.out.println(message.getPayload());
            System.out.println("======================================\n");

            JsonNode root = objectMapper.readTree(message.getPayload());

            Integer agvId = root.path("agvId").asInt(0);

            if (agvId == 0) {
                send(session, new ErrorMessage(
                        "ERROR",
                        null,
                        "필수 값 누락: agvId"
                ));
                return;
            }

            agvSessionHandler.addSession(agvId, session);
            agvService.markConnected(agvId);

            AgvStatusMessage statusMessage =
                    objectMapper.treeToValue(
                            root,
                            AgvStatusMessage.class
                    );

            handleStatusReport(
                    session,
                    statusMessage
            );

        } catch (Exception e) {
            System.out.println("[WS MESSAGE ERROR]");
            System.out.println("reason=" + e.getMessage());

            try {
                send(session, new ErrorMessage(
                        "ERROR",
                        null,
                        "메시지 처리 실패: " + e.getMessage()
                ));
            } catch (Exception ignored) {
            }
        }
    }

    private void handleStatusReport(
            WebSocketSession session,
            AgvStatusMessage message
    ) throws Exception {

        printStatusLog(message);

        broadcastAgvStatus(message);

        if (Boolean.TRUE.equals(message.getHasImage())
                && message.getImage() != null
                && !message.getImage().isBlank()) {

            handleImageMessage(session, message);
        }

        if ("DONE".equals(message.getEvent())) {
            handleDoneEvent(session, message);
        }
    }

    private void printStatusLog(AgvStatusMessage message) {
        System.out.println("\n========== [AGV STATUS PARSED] ==========");
        System.out.println("agvId       = " + message.getAgvId());
        System.out.println("timestamp   = " + message.getTimestamp());
        System.out.println("status      = " + message.getStatus());
        System.out.println("event       = " + message.getEvent());
        System.out.println("taskId      = " + message.getTaskId());
        System.out.println("commandId   = " + message.getCommandId());
        System.out.println("located     = " + message.getLocated());
        System.out.println("destination = " + message.getDestination());
        System.out.println("cargo       = " + message.getCargo());
        System.out.println("isCW        = " + message.getIsCW());
        System.out.println("hasImage    = " + message.getHasImage());
        System.out.println("image       = "
                + (message.getImage() == null
                ? null
                : "base64 length=" + message.getImage().length()));
        System.out.println("========================================\n");
    }

    private void broadcastAgvStatus(AgvStatusMessage message) {
        Map<String, Object> data = new HashMap<>();
        data.put("agvId", message.getAgvId());
        data.put("status", message.getStatus());
        data.put("event", message.getEvent());
        data.put("taskId", message.getTaskId());
        data.put("commandId", message.getCommandId());
        data.put("located", message.getLocated());
        data.put("destination", message.getDestination());
        data.put("cargo", message.getCargo());
        data.put("isCW", message.getIsCW());
        data.put("timestamp", message.getTimestamp());

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "AGV_STATUS");
        payload.put("data", data);

        dashboardSender.broadcast(payload);
    }

    private void handleImageMessage(
            WebSocketSession session,
            AgvStatusMessage message
    ) throws Exception {

        System.out.println("[VISION] image received. agvId="
                + message.getAgvId()
                + ", base64Length="
                + message.getImage().length());

        try {
            ArucoResultMessage arucoResult =
                    arucoService.detectFromBase64(
                            message.getAgvId(),
                            message.getImage()
                    );

            if (arucoResult != null) {
                System.out.println("[VISION RESULT] agvId="
                        + arucoResult.getAgvId()
                        + ", detected="
                        + arucoResult.getDetected()
                        + ", markerCount="
                        + arucoResult.getMarkerCount());

                if (arucoResult.getMarkers() != null) {
                    for (ArucoResultMessage.MarkerInfo marker : arucoResult.getMarkers()) {
                        System.out.println("  markerId="
                                + marker.getMarkerId()
                                + ", distance="
                                + marker.getDistance()
                                + ", yaw="
                                + marker.getYaw()
                                + ", pitch="
                                + marker.getPitch()
                                + ", centered="
                                + marker.getCentered());
                    }
                }

                send(session, arucoResult);

            } else {
                System.out.println("[VISION FAIL] marker not detected. agvId="
                        + message.getAgvId());

                send(session, new ErrorMessage(
                        "ERROR",
                        message.getAgvId(),
                        "마커 인식 실패"
                ));
            }

        } catch (Exception e) {
            System.out.println("[VISION ERROR] " + e.getMessage());

            send(session, new ErrorMessage(
                    "ERROR",
                    message.getAgvId(),
                    "이미지 처리 실패"
            ));
        }
    }

    private void handleDoneEvent(
            WebSocketSession session,
            AgvStatusMessage message
    ) throws Exception {

        System.out.println("[AGV DONE EVENT] agvId="
                + message.getAgvId()
                + ", commandId="
                + message.getCommandId());

        if (message.getCommandId() == null) {
            send(session, new ErrorMessage(
                    "ERROR",
                    message.getAgvId(),
                    "DONE 이벤트에는 commandId가 필요합니다."
            ));
            return;
        }

        if (chipScenarioTestService.isRunning()) {
            chipScenarioTestService.handleAgvDone(message);
            return;
        }

        try {
            missionResultService.handleAgvDone(message);
        } catch (IllegalArgumentException e) {
            send(session, new ErrorMessage(
                    "ERROR",
                    message.getAgvId(),
                    "서버가 관리 중인 Mission을 찾을 수 없습니다. commandId="
                            + message.getCommandId()
            ));
        }
    }

    private void send(WebSocketSession session, Object response) throws Exception {
        if (response == null) {
            return;
        }

        session.sendMessage(
                new TextMessage(objectMapper.writeValueAsString(response))
        );
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        Integer agvId =
                agvSessionHandler.findAgvIdBySession(session);

        agvSessionHandler.removeSession(session);

        if (agvId != null) {
            agvService.markDisconnected(agvId);
            expectedCommandIds.remove(agvId);

            System.out.println("AGV 연결 종료: agvId=" + agvId);
        } else {
            System.out.println("AGV 연결 종료: sessionId=" + session.getId());
        }
    }
}