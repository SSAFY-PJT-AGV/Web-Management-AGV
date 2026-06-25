package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.material.repository.MaterialRepository;
import com.example.ssafy_pjt.backend.feature.mission.dto.MissionResponse;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import com.example.ssafy_pjt.backend.feature.zone.repository.ZoneRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardSender;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.enums.EventLevel;
import com.example.ssafy_pjt.backend.feature.event.enums.EventType;
import com.example.ssafy_pjt.backend.feature.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReplenishmentService {

    private static final int REPLENISHMENT_BOX_QUANTITY = 4;

    private final MissionRepository missionRepository;
    private final MaterialRepository materialRepository;
    private final ZoneRepository zoneRepository;
    private final DashboardSender dashboardSender;
    private final EventLogService eventLogService;

    @Transactional
    public void createReplenishmentMissions(String materialCode, int quantity) {
        int replenishQuantity = REPLENISHMENT_BOX_QUANTITY;

        Material material = materialRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 자재입니다: " + materialCode
                ));

        Zone inbound = zoneRepository.findByZoneName("INBOUND")
                .orElseThrow(() -> new IllegalArgumentException("입고 구역이 없습니다."));

        Zone crossZone = zoneRepository.findByZoneName("CROSS_ZONE")
                .orElseThrow(() -> new IllegalArgumentException("교차구역이 없습니다."));

        Zone materialStorage = zoneRepository.findByZoneName("MATERIAL_BOX_STORAGE")
                .orElseThrow(() -> new IllegalArgumentException("자재 보관 구역이 없습니다."));

        Zone outbound = zoneRepository.findByZoneName("OUTBOUND")
                .orElseThrow(() -> new IllegalArgumentException("입출고 구역이 없습니다."));

        int sequence = getNextSequenceOrder();

        // AGV02: 입고 구역에서 보급 자재 상자 픽업
        createMission(
                MissionType.PICK_FROM_INBOUND,
                material,
                replenishQuantity,
                inbound,
                null,
                sequence++
        );

        // AGV02: 보급 자재 상자를 교차구역에 하역
        createMission(
                MissionType.DROP_TO_CROSS,
                material,
                replenishQuantity,
                inbound,
                crossZone,
                sequence++
        );

        // AGV01: 자재 보관 구역에서 기존 빈 자재 상자 픽업
        createMission(
                MissionType.PICK_EMPTY_BOX,
                material,
                0,
                materialStorage,
                null,
                sequence++
        );

        // AGV01: 빈 자재 상자를 교차구역에 하역
        createMission(
                MissionType.DROP_TO_CROSS,
                material,
                0,
                materialStorage,
                crossZone,
                sequence++
        );

        // AGV01: 교차구역에서 보급 자재 상자 픽업
        createMission(
                MissionType.PICK_FROM_CROSS,
                material,
                replenishQuantity,
                crossZone,
                materialStorage,
                sequence++
        );

        // AGV01: 보급 자재 상자를 자재 보관 구역에 하역
        createMission(
                MissionType.DROP_TO_STORAGE,
                material,
                replenishQuantity,
                crossZone,
                materialStorage,
                sequence++
        );

        // AGV02: 교차구역에서 AGV01이 놓은 빈 자재 상자 회수
        createMission(
                MissionType.PICK_FROM_CROSS,
                material,
                0,
                crossZone,
                outbound,
                sequence++
        );

        // AGV02: 빈 자재 상자를 입출고 구역에 반납
        createMission(
                MissionType.DROP_EMPTY_BOX,
                material,
                0,
                crossZone,
                outbound,
                sequence
        );

        eventLogService.create(
                EventLog.create(
                        EventType.MISSION_CREATED,
                        EventLevel.INFO,
                        "자재 보급 Mission 생성 완료: material="
                                + materialCode
                                + ", quantity="
                                + replenishQuantity,
                        "MATERIAL",
                        material.getMaterialId()
                )
        );
    }

    private void createMission(
            MissionType missionType,
            Material material,
            int quantity,
            Zone sourceZone,
            Zone targetZone,
            int sequenceOrder
    ) {
        Mission mission = new Mission();

        mission.setTask(null);
        mission.setAgv(null);
        mission.setMissionType(missionType);
        mission.setMaterial(material);
        mission.setStatus(MissionStatus.CREATED);
        mission.setSequenceOrder(sequenceOrder);
        mission.setQuantity(quantity);
        mission.setSourceZone(sourceZone);
        mission.setTargetZone(targetZone);
        mission.setCreatedAt(LocalDateTime.now());

        Mission saved = missionRepository.save(mission);

        broadcastMissionUpdated(saved);
    }

    private void broadcastMissionUpdated(Mission mission) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MISSION_UPDATED");
        payload.put("data", new MissionResponse(mission));

        dashboardSender.broadcast(payload);
    }

    private int getNextSequenceOrder() {
        return missionRepository.findMaxSequenceOrder()
                .orElse(0) + 1;
    }
}