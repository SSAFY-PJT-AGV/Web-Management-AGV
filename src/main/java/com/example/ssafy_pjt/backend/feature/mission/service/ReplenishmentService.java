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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReplenishmentService {

    private final MissionRepository missionRepository;
    private final MaterialRepository materialRepository;
    private final ZoneRepository zoneRepository;
    private final DashboardSender dashboardSender;

    @Transactional
    public void createReplenishmentMissions(String materialCode, int quantity) {
        Material material = materialRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자재입니다: " + materialCode));

        Zone inbound = zoneRepository.findByZoneName("INBOUND")
                .orElseThrow(() -> new IllegalArgumentException("입고 구역이 없습니다."));

        Zone crossZone = zoneRepository.findByZoneName("CROSS_ZONE")
                .orElseThrow(() -> new IllegalArgumentException("교차구역이 없습니다."));

        Zone materialStorage = zoneRepository.findByZoneName("MATERIAL_BOX_STORAGE")
                .orElseThrow(() -> new IllegalArgumentException("자재 보관 구역이 없습니다."));

        Zone outbound = zoneRepository.findByZoneName("OUTBOUND")
                .orElseThrow(() -> new IllegalArgumentException("입출고 구역이 없습니다."));

        int sequence = getNextSequenceOrder();

        createMission(MissionType.PICK_FROM_INBOUND, material, quantity, inbound, null, sequence++);
        createMission(MissionType.DROP_TO_CROSS, material, quantity, inbound, crossZone, sequence++);
        createMission(MissionType.DROP_TO_CROSS, null, 0, materialStorage, crossZone, sequence++);
        createMission(MissionType.PICK_FROM_CROSS, material, quantity, crossZone, null, sequence++);
        createMission(MissionType.DROP_TO_STORAGE, material, quantity, crossZone, materialStorage, sequence++);
        createMission(MissionType.PICK_EMPTY_BOX, null, 0, crossZone, null, sequence++);
        createMission(MissionType.DROP_EMPTY_BOX, null, 0, crossZone, outbound, sequence);
    }

    private record MissionScore(Mission mission, int score) {
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