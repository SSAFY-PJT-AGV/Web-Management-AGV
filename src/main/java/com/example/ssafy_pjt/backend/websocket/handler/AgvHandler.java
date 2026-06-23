package com.example.ssafy_pjt.backend.websocket.handler;

import com.example.ssafy_pjt.backend.feature.agv.service.AgvService;
import com.example.ssafy_pjt.backend.feature.agv.service.ChipScenarioTestService;
import com.example.ssafy_pjt.backend.feature.marker.service.ArucoService;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionDispatchService;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionResultService;
import com.example.ssafy_pjt.backend.feature.scale.service.AgvPerformanceMetricService;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.dto.ArucoResultMessage;
import com.example.ssafy_pjt.backend.websocket.dto.ErrorMessage;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardSender;
import com.example.ssafy_pjt.backend.websocket.session.AgvSessionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@RequiredArgsConstructor
public class AgvHandler extends TextWebSocketHandler {

    private static final long DB_UPDATE_INTERVAL_MS = 1000;
    private static final int WORKER_COUNT = 4;
    private static final int QUEUE_CAPACITY = 5000;

    private final ArucoService arucoService;
    private final AgvSessionHandler agvSessionHandler;
    private final AgvService agvService;
    private final ChipScenarioTestService chipScenarioTestService;
    private final MissionResultService missionResultService;
    private final MissionDispatchService missionDispatchService;
    private final ObjectMapper objectMapper;
    private final DashboardSender dashboardSender;
    private final AgvPerformanceMetricService metricService;

    private final Map<Integer, Long> expectedCommandIds = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastDbUpdateAt = new ConcurrentHashMap<>();

    private final List<BlockingQueue<AgvStatusJob>> workerQueues = new ArrayList<>();
    private ExecutorService workerExecutor;
    private volatile boolean running = true;

    @PostConstruct
    public void startWorkers() {
        workerExecutor = Executors.newFixedThreadPool(WORKER_COUNT);

        for (int i = 0; i < WORKER_COUNT; i++) {
            BlockingQueue<AgvStatusJob> queue =
                    new LinkedBlockingQueue<>(QUEUE_CAPACITY);

            workerQueues.add(queue);

            int workerId = i;

            workerExecutor.submit(() -> runWorker(workerId, queue));
        }

        System.out.println("[AGV QUEUE] workers started. count=" + WORKER_COUNT);
    }

    @PreDestroy
    public void stopWorkers() {
        running = false;

        if (workerExecutor != null) {
            workerExecutor.shutdownNow();
        }

        System.out.println("[AGV QUEUE] workers stopped.");
    }

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
        long start = metricService.start();

        try {
            JsonNode root = objectMapper.readTree(message.getPayload());

            Integer agvId = root.path("agvId").asInt(0);

            if (agvId == 0) {
                metricService.receiveFail();

                send(session, new ErrorMessage(
                        "ERROR",
                        null,
                        "필수 값 누락: agvId"
                ));
                return;
            }

            agvSessionHandler.addSession(agvId, session);

            AgvStatusMessage statusMessage =
                    objectMapper.treeToValue(
                            root,
                            AgvStatusMessage.class
                    );

            boolean offered =
                    getQueue(agvId).offer(
                            new AgvStatusJob(
                                    session,
                                    statusMessage,
                                    start
                            )
                    );

            if (!offered) {
                metricService.queueReject();

                send(session, new ErrorMessage(
                        "ERROR",
                        agvId,
                        "AGV 상태 처리 큐가 가득 찼습니다."
                ));
            }

        } catch (Exception e) {
            metricService.receiveFail();

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

    private BlockingQueue<AgvStatusJob> getQueue(Integer agvId) {
        int index =
                Math.floorMod(agvId, WORKER_COUNT);

        return workerQueues.get(index);
    }

    private void runWorker(
            int workerId,
            BlockingQueue<AgvStatusJob> queue
    ) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                AgvStatusJob job = queue.take();

                handleStatusReport(
                        job.session(),
                        job.message()
                );

                metricService.success(job.startTime());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            } catch (Exception e) {
                metricService.workerFail();

                System.out.println("[AGV QUEUE WORKER ERROR]");
                System.out.println("workerId=" + workerId);
                System.out.println("reason=" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleStatusReport(
            WebSocketSession session,
            AgvStatusMessage message
    ) throws Exception {

        printImportantLog(message);

        /*
         * AGV STATUS는 10FPS로 들어오는 Telemetry 데이터입니다.
         * 실시간 관제는 Dashboard WebSocket으로 유지하고,
         * DB write는 AGV별 1초 1회 또는 중요 이벤트에만 수행합니다.
         */
        if (isImportantEvent(message) || shouldUpdateDb(message.getAgvId())) {
            agvService.updateStatus(message);
        }

        broadcastAgvStatus(message);

        if (Boolean.TRUE.equals(message.getHasImage())
                && message.getImage() != null
                && !message.getImage().isBlank()) {

            handleImageMessage(session, message);
        }

        /*
         * Mission Flow에 영향을 주는 이벤트만 Scheduler / Mission Result 처리
         */
        if ("DONE".equals(message.getEvent())) {
            handleDoneEvent(session, message);

            missionDispatchService.assignCreatedMissionsToAgvQueues();
            missionDispatchService.dispatchNextMission(message.getAgvId());
            return;
        }

        if ("IDLE".equals(message.getStatus())) {
            missionDispatchService.assignCreatedMissionsToAgvQueues();
            missionDispatchService.dispatchNextMission(message.getAgvId());
        }

        if ("ERROR".equals(message.getStatus())) {
            System.out.println("[AGV ERROR STATUS] agvId="
                    + message.getAgvId()
                    + ", commandId="
                    + message.getCommandId());
        }
    }

    private boolean isImportantEvent(AgvStatusMessage message) {
        return "DONE".equals(message.getEvent())
                || "IDLE".equals(message.getStatus())
                || "ERROR".equals(message.getStatus())
                || "STOP".equals(message.getStatus());
    }

    private boolean shouldUpdateDb(Integer agvId) {
        long now = System.currentTimeMillis();

        Long lastUpdateAt = lastDbUpdateAt.get(agvId);

        if (lastUpdateAt == null
                || now - lastUpdateAt >= DB_UPDATE_INTERVAL_MS) {

            lastDbUpdateAt.put(agvId, now);
            return true;
        }

        return false;
    }

    private void printImportantLog(AgvStatusMessage message) {
        boolean important =
                isImportantEvent(message)
                        || Boolean.TRUE.equals(message.getHasImage());

        if (!important) {
            return;
        }

        System.out.println("\n========== [AGV IMPORTANT STATUS] ==========");
        System.out.println("agvId       = " + message.getAgvId());
        System.out.println("timestamp   = " + message.getTimestamp());
        System.out.println("status      = " + message.getStatus());
        System.out.println("event       = " + message.getEvent());
        System.out.println("taskId      = " + message.getTaskId());
        System.out.println("commandId   = " + message.getCommandId());
        System.out.println("located     = " + message.getLocated());
        System.out.println("destination = " + message.getDestination());
        System.out.println("cargo       = " + message.getCargo());
        System.out.println("hasImage    = " + message.getHasImage());
        System.out.println("image       = "
                + (message.getImage() == null
                ? null
                : "base64 length=" + message.getImage().length()));
        System.out.println("============================================\n");
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
        data.put("timestamp", message.getTimestamp());

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "AGV_STATUS");
        payload.put("data", data);

        dashboardSender.broadcast(payload);
    }

    private void handleImageMessage(
            WebSocketSession session,
            AgvStatusMessage message
    ) {

        System.out.println("[VISION] image received. agvId="
                + message.getAgvId()
                + ", base64Length="
                + message.getImage().length());

        arucoService.submitFrame(
                message.getAgvId(),
                message.getImage(),
                arucoResult -> {
                    try {
                        if (arucoResult == null) {
                            return;
                        }

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

                        synchronized (session) {
                            send(session, arucoResult);
                        }

                    } catch (Exception e) {
                        metricService.sendFail();

                        System.out.println("[VISION SEND ERROR] agvId="
                                + message.getAgvId()
                                + ", reason="
                                + e.getMessage());
                    }
                }
        );
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

        synchronized (session) {
            session.sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(response))
            );
        }
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
            expectedCommandIds.remove(agvId);
            lastDbUpdateAt.remove(agvId);

            System.out.println(
                    "AGV 세션 종료: agvId=" + agvId
            );
        }
    }

    private record AgvStatusJob(
            WebSocketSession session,
            AgvStatusMessage message,
            long startTime
    ) {
    }
}