package com.example.ssafy_pjt.backend.feature.demo_scenario;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.enums.EventLevel;
import com.example.ssafy_pjt.backend.feature.event.enums.EventType;
import com.example.ssafy_pjt.backend.feature.event.service.EventLogService;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.product.enums.ProductType;
import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskStatus;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskType;
import com.example.ssafy_pjt.backend.feature.task.repository.ProductionTaskRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class DemoScenarioService {

    private static final String CARGO_NONE = "NONE";

    private final AgvRepository agvRepository;
    private final MissionRepository missionRepository;
    private final ArucoMarkerRepository arucoMarkerRepository;
    private final EventLogService eventLogService;
    private final DashboardBroadcastService dashboardBroadcastService;
    private final PlatformTransactionManager transactionManager;
    private final InventoryRepository inventoryRepository;
    private final ProductionTaskRepository productionTaskRepository;

    private ScheduledExecutorService executor;
    private Long demoTaskId;

    private final List<Long> demoMissionIds = new CopyOnWriteArrayList<>();

    public synchronized void start() {
        stop();

        executor = Executors.newSingleThreadScheduledExecutor();

        schedule(0, this::initScenario);

        schedule(1, () -> startMission(1, MissionType.PICK_FROM_STORAGE, 6, 17, null, "AGV01 자재 픽업 시작"));
        schedule(4, () -> move(1, 17, 17, null));
        schedule(7, () -> completeMission(1, "LOADING", 18, 17, 1, "AGV01 자재 픽업 완료"));
        schedule(8, () -> startMission(1, MissionType.DROP_TO_CONVEYOR, 18, 19, 1, "AGV01 컨베이어 하역 이동"));
        schedule(13, () -> completeMission(1, "UNLOADING", 19, 19, null, "AGV01 컨베이어 하역 완료"));

        schedule(1, () -> startMission(2, MissionType.PICK_FROM_INBOUND, 5, 11, null, "AGV02 꽉 찬 상자 픽업 시작"));
        schedule(5, () -> completeMission(2, "LOADING", 11, 11, 2, "AGV02 꽉 찬 상자 픽업 완료"));
        schedule(6, () -> startMission(2, MissionType.DROP_TO_CROSS, 11, 14, 2, "AGV02 교차구역 하역 이동"));
        schedule(9, () -> move(2, 12, 14, 2));
        schedule(12, () -> move(2, 13, 14, 2));
        schedule(16, () -> completeMission(2, "UNLOADING", 14, 14, null, "AGV02 교차구역 하역 완료"));

        schedule(17, () -> startMission(1, MissionType.PICK_EMPTY_BOX, 19, 17, null, "AGV01 빈 상자 픽업 시작"));
        schedule(22, () -> move(1, 18, 17, null));
        schedule(27, () -> move(1, 17, 17, null));
        schedule(31, () -> completeMission(1, "LOADING", 16, 17, 6, "AGV01 빈 상자 픽업 완료"));
        schedule(32, () -> startMission(1, MissionType.DROP_EMPTY_BOX, 16, 14, 6, "AGV01 빈 상자 교차구역 전달 시작"));
        schedule(38, () -> move(1, 15, 14, 6));
        schedule(43, () -> completeMission(1, "UNLOADING", 14, 14, null, "AGV01 빈 상자 교차구역 전달 완료"));

        schedule(17, () -> startMission(2, MissionType.PICK_FROM_CONVEYOR, 20, 20, null, "AGV02 완제품 부품 픽업 시작"));
        schedule(23, () -> completeMission(2, "LOADING", 20, 20, 1, "AGV02 완제품 부품 픽업 완료"));
        schedule(24, () -> startMission(2, MissionType.DROP_TO_FINISHED_BOX_STORAGE, 20, 22, 1, "AGV02 완제품 상자 적재 이동"));
        schedule(34, () -> move(2, 21, 22, 1));
        schedule(47, () -> {
            completeMission(2, "UNLOADING", 22, 22, null, "AGV02 완제품 상자 완성 및 보관 완료");

            tx(() -> {
                if (demoTaskId != null) {
                    ProductionTask task = productionTaskRepository.findById(demoTaskId)
                            .orElseThrow();

                    task.setStatus(TaskStatus.DONE);
                    task.setCompletedAt(LocalDateTime.now());
                }

                event("카메라 센서 모듈 생산 작업 완료", null);
                refreshAll();
            });
        });

        schedule(48, () -> startMission(1, MissionType.PICK_FROM_CROSS, 14, 14, null, "AGV01 교차구역 꽉 찬 상자 회수 시작"));
        schedule(51, () -> completeMission(1, "LOADING", 14, 14, 2, "AGV01 꽉 찬 상자 픽업 완료"));
        schedule(52, () -> startMission(1, MissionType.DROP_TO_STORAGE, 14, 17, 2, "AGV01 자재 보관 구역 복귀"));
        schedule(54, () -> move(1, 15, 17, 2));
        schedule(56, () -> move(1, 16, 17, 2));
        schedule(59, () -> {
            completeMission(1, "UNLOADING", 17, 17, null, "AGV01 자재 보관 구역 하역 완료");

            tx(() -> {
                setChipInventory(4, InventoryStatus.NORMAL);
                event("제어칩 재고 보충 완료: 0개 → 4개", null);
                refreshAll();
            });
        });

        schedule(48, () -> startMission(2, MissionType.PICK_EMPTY_BOX, 11, 14, null, "AGV02 빈 상자 회수 시작"));
        schedule(51, () -> move(2, 12, 14, null));
        schedule(53, () -> move(2, 13, 14, null));
        schedule(55, () -> completeMission(2, "LOADING", 14, 14, 6, "AGV02 빈 상자 픽업 완료"));
        schedule(56, () -> startMission(2, MissionType.DROP_EMPTY_BOX, 14, 11, 6, "AGV02 입고 구역 빈 상자 하역 이동"));
        schedule(57, () -> move(2, 13, 11, 6));
        schedule(58, () -> move(2, 12, 11, 6));
        schedule(60, () -> completeMission(2, "UNLOADING", 11, 11, null, "AGV02 입고 구역 빈 상자 하역 완료"));

        schedule(61, this::finishScenario);
    }

    public boolean isRunning() {
        return executor != null && !executor.isShutdown();
    }

    public synchronized void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public void reset() {
        stop();

        tx(() -> {
            List<Mission> missions = missionRepository.findAllById(demoMissionIds);

            for (Mission mission : missions) {
                mission.setStatus(MissionStatus.CANCELLED);
                mission.setCompletedAt(LocalDateTime.now());
            }

            if (demoTaskId != null) {
                productionTaskRepository.findById(demoTaskId)
                        .ifPresent(task -> {
                            task.setStatus(TaskStatus.CANCELLED);
                            task.setCompletedAt(LocalDateTime.now());
                        });
            }

            resetAgv(getAgv(1));
            resetAgv(getAgv(2));

            event("시연 시나리오 초기화", null);

            refreshAll();
            broadcastAgv(1, "IDLE", null, null, null, null);
            broadcastAgv(2, "IDLE", null, null, null, null);
        });

        demoMissionIds.clear();
        demoTaskId = null;
    }

    private void initScenario() {
        tx(() -> {
            demoMissionIds.clear();

            Agv agv1 = getAgv(1);
            Agv agv2 = getAgv(2);

            agv1.setTestMode(true);
            agv2.setTestMode(true);

            resetAgv(agv1);
            resetAgv(agv2);

            setChipInventory(0, InventoryStatus.SHORTAGE);

            ProductionTask task = ProductionTask.builder()
                    .productType(ProductType.CAMERA_MODULE)
                    .quantity(1)
                    .taskType(TaskType.PRODUCTION)
                    .status(TaskStatus.RUNNING)
                    .startedAt(LocalDateTime.now())
                    .build();

            productionTaskRepository.save(task);
            demoTaskId = task.getTaskId();

            List<Mission> missions = List.of(
                    createMission(agv1, MissionType.PICK_FROM_STORAGE, 1, task),
                    createMission(agv1, MissionType.DROP_TO_CONVEYOR, 2, task),
                    createMission(agv1, MissionType.PICK_EMPTY_BOX, 3, task),
                    createMission(agv1, MissionType.DROP_EMPTY_BOX, 4, task),
                    createMission(agv1, MissionType.PICK_FROM_CROSS, 5, task),
                    createMission(agv1, MissionType.DROP_TO_STORAGE, 6, task),

                    createMission(agv2, MissionType.PICK_FROM_INBOUND, 1, task),
                    createMission(agv2, MissionType.DROP_TO_CROSS, 2, task),
                    createMission(agv2, MissionType.PICK_FROM_CONVEYOR, 3, task),
                    createMission(agv2, MissionType.DROP_TO_FINISHED_BOX_STORAGE, 4, task),
                    createMission(agv2, MissionType.PICK_EMPTY_BOX, 5, task),
                    createMission(agv2, MissionType.DROP_EMPTY_BOX, 6, task)
            );

            missionRepository.saveAll(missions);

            for (Mission mission : missions) {
                demoMissionIds.add(mission.getMissionId());
            }

            event("제어칩 재고 부족 감지", null);
            event("시연 시나리오 시작: 자동 물류 사이클 생성", null);

            refreshAll();
            broadcastAgv(1, "IDLE", null, null, null, null);
            broadcastAgv(2, "IDLE", null, null, null, null);
        });
    }

    private Mission createMission(
            Agv agv,
            MissionType type,
            int sequence,
            ProductionTask task
    ) {
        Mission mission = new Mission();

        mission.setAgv(agv);
        mission.setTask(task);
        mission.setMissionType(type);
        mission.setStatus(MissionStatus.CREATED);
        mission.setSequenceOrder(sequence);
        mission.setQuantity(1);

        return mission;
    }

    private void startMission(
            Integer agvId,
            MissionType type,
            Integer located,
            Integer destination,
            Integer cargoMarkerId,
            String message
    ) {
        tx(() -> {
            Agv agv = getAgv(agvId);
            Mission mission = findNextCreatedMission(agvId, type);

            mission.setStatus(MissionStatus.IN_PROGRESS);
            mission.setStartedAt(LocalDateTime.now());

            agv.setStatus(AgvStatus.MOVING);
            agv.setCurrentMission(mission);
            agv.setLastSeenAt(LocalDateTime.now());

            setCurrentMarker(agv, located);

            event(message, mission.getMissionId());

            refreshAll();
            broadcastAgv(agvId, "MOVING", mission.getMissionId(), located, destination, cargoMarkerId);
        });
    }

    private void move(
            Integer agvId,
            Integer located,
            Integer destination,
            Integer cargoMarkerId
    ) {
        tx(() -> {
            Agv agv = getAgv(agvId);
            Mission mission = agv.getCurrentMission();

            agv.setStatus(AgvStatus.MOVING);
            agv.setLastSeenAt(LocalDateTime.now());

            setCurrentMarker(agv, located);

            Long commandId = mission == null ? null : mission.getMissionId();

            broadcastAgv(agvId, "MOVING", commandId, located, destination, cargoMarkerId);
        });
    }

    private void completeMission(
            Integer agvId,
            String status,
            Integer located,
            Integer destination,
            Integer cargoMarkerId,
            String message
    ) {
        tx(() -> {
            Agv agv = getAgv(agvId);
            Mission mission = agv.getCurrentMission();

            if (mission == null) {
                return;
            }

            agv.setStatus(resolveAgvStatus(status));
            agv.setLastSeenAt(LocalDateTime.now());

            setCurrentMarker(agv, located);

            mission.setStatus(MissionStatus.COMPLETED);
            mission.setCompletedAt(LocalDateTime.now());

            agv.setStatus(AgvStatus.IDLE);
            agv.setCurrentMission(null);
            agv.setLastSeenAt(LocalDateTime.now());

            event(message, mission.getMissionId());

            refreshAll();

            broadcastAgv(
                    agvId,
                    status,
                    mission.getMissionId(),
                    located,
                    destination,
                    cargoMarkerId
            );
        });
    }

    private void finishScenario() {
        tx(() -> {
            resetAgv(getAgv(1));
            resetAgv(getAgv(2));

            event("시연 완료: 두 AGV 작업 사이클 종료", null);

            refreshAll();
            broadcastAgv(1, "IDLE", null, 17, null, null);
            broadcastAgv(2, "IDLE", null, 11, null, null);
        });

        stop();
    }

    private Mission findNextCreatedMission(Integer agvId, MissionType type) {
        return missionRepository.findAllById(demoMissionIds)
                .stream()
                .filter(m -> Objects.equals(m.getAgv().getAgvId(), agvId))
                .filter(m -> m.getMissionType() == type)
                .filter(m -> m.getStatus() == MissionStatus.CREATED)
                .min(Comparator.comparing(Mission::getSequenceOrder))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "시나리오 Mission 없음. agvId="
                                        + agvId
                                        + ", type="
                                        + type
                        )
                );
    }

    private AgvStatus resolveAgvStatus(String status) {
        return switch (status) {
            case "LOADING" -> AgvStatus.LOADING;
            case "UNLOADING" -> AgvStatus.UNLOADING;
            case "ARRIVED" -> AgvStatus.ARRIVED;
            case "WAITING" -> AgvStatus.WAITING;
            case "STOP" -> AgvStatus.STOP;
            case "ERROR" -> AgvStatus.ERROR;
            case "IDLE" -> AgvStatus.IDLE;
            default -> AgvStatus.MOVING;
        };
    }

    private void setCurrentMarker(Agv agv, Integer markerId) {
        if (markerId == null) {
            return;
        }

        ArucoMarker marker = arucoMarkerRepository.findById(markerId)
                .orElse(null);

        if (marker != null) {
            agv.setCurrentMarker(marker);
        }
    }

    private String cargoName(Integer markerId) {
        if (markerId == null) {
            return CARGO_NONE;
        }

        return arucoMarkerRepository.findById(markerId)
                .map(ArucoMarker::getDescription)
                .map(this::normalizeCargoName)
                .orElse("UNKNOWN");
    }

    private String normalizeCargoName(String description) {
        if (description == null || description.isBlank()) {
            return "UNKNOWN";
        }

        return description
                .replace(" 타입 마커", "")
                .replace(" 부품 상자 마커", " 부품 상자")
                .replace(" 완제품 상자 마커", " 완제품 상자")
                .replace(" 빈 완제품 상자 1", " 빈 상자")
                .replace(" 빈 완제품 상자 2", " 빈 상자")
                .replace("마커", "")
                .trim();
    }

    private void resetAgv(Agv agv) {
        agv.setStatus(AgvStatus.IDLE);
        agv.setCurrentMission(null);
        agv.setLastSeenAt(LocalDateTime.now());
    }

    private Agv getAgv(Integer agvId) {
        return agvRepository.findById(agvId)
                .orElseThrow(() ->
                        new IllegalArgumentException("AGV 없음: " + agvId)
                );
    }

    private void setChipInventory(
            int quantity,
            InventoryStatus status
    ) {
        Inventory inventory = inventoryRepository.findByMaterial_MaterialId(1L)
                .orElseThrow(() ->
                        new IllegalStateException("제어칩 재고 정보를 찾을 수 없습니다.")
                );

        inventory.setCurrentQuantity(quantity);
        inventory.setReservedQuantity(0);
        inventory.setStatus(status);
        inventory.setUpdatedAt(LocalDateTime.now());
    }

    private void event(String message, Long missionId) {
        eventLogService.create(
                EventLog.create(
                        EventType.STATUS_CHANGED,
                        EventLevel.INFO,
                        message,
                        "DEMO_SCENARIO",
                        missionId
                )
        );
    }

    private void broadcastAgv(
            Integer agvId,
            String status,
            Long commandId,
            Integer located,
            Integer destination,
            Integer cargoMarkerId
    ) {
        String payload = cargoName(cargoMarkerId);

        Map<String, Object> data = new HashMap<>();

        data.put("agvId", agvId);
        data.put("status", status);
        data.put("commandId", commandId);

        data.put("located", located);
        data.put("destination", destination);

        data.put("currentMarkerId", located);
        data.put("currentMarker", located);

        data.put("destinationMarkerId", destination);
        data.put("destinationMarker", destination);
        data.put("targetMarkerId", destination);

        data.put("cargo", payload);
        data.put("cargoType", payload);
        data.put("cargoMaterialCode", payload);

        data.put("timestamp", LocalDateTime.now().toString());

        dashboardBroadcastService.agvStatus(data);
    }

    private void refreshAll() {
        dashboardBroadcastService.taskRefresh();
        dashboardBroadcastService.missionRefresh();
        dashboardBroadcastService.eventRefresh();
        dashboardBroadcastService.inventoryRefresh();
        dashboardBroadcastService.mapRefresh();
    }

    private void schedule(long delaySec, Runnable runnable) {
        executor.schedule(runnable, delaySec, TimeUnit.SECONDS);
    }

    private void tx(Runnable runnable) {
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> runnable.run());
    }
}