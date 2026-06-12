package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FactoryStateSummaryService {

    private final InventoryRepository inventoryRepository;
    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;

    public String buildSummary() {
        return """
                [FACTORY STATE]
                
                Inventory:
                %s
                
                Mission:
                CREATED=%d
                
                AGV:
                %s
                """.formatted(
                buildInventorySummary(),
                missionRepository.countByStatus(MissionStatus.CREATED),
                buildAgvSummary()
        );
    }

    private String buildInventorySummary() {
        List<Inventory> inventories = inventoryRepository.findAll();

        return inventories.stream()
                .map(inventory -> {
                    int available =
                            inventory.getCurrentQuantity()
                                    - inventory.getReservedQuantity();

                    return "- %s: current=%d, reserved=%d, available=%d, min=%d, status=%s"
                            .formatted(
                                    inventory.getMaterial().getMaterialCode(),
                                    inventory.getCurrentQuantity(),
                                    inventory.getReservedQuantity(),
                                    available,
                                    inventory.getMinThreshold(),
                                    inventory.getStatus()
                            );
                })
                .toList()
                .toString();
    }

    private String buildAgvSummary() {
        List<Agv> agvs = agvRepository.findAll();

        return agvs.stream()
                .map(agv -> "- AGV%02d: role=%s, status=%s, currentMission=%s"
                        .formatted(
                                agv.getAgvId(),
                                agv.getRole(),
                                agv.getStatus(),
                                agv.getCurrentMission() == null
                                        ? "none"
                                        : agv.getCurrentMission().getMissionId()
                        ))
                .toList()
                .toString();
    }
}