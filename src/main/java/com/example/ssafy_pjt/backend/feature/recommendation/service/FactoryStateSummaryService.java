package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.material.entity.ProductMaterial;
import com.example.ssafy_pjt.backend.feature.material.repository.ProductMaterialRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskStatus;
import com.example.ssafy_pjt.backend.feature.task.repository.ProductionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FactoryStateSummaryService {

    private final InventoryRepository inventoryRepository;
    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;
    private final ProductionTaskRepository taskRepository;
    private final ProductMaterialRepository productMaterialRepository;

    public String buildSummary() {
        return """
                [FACTORY STATE]

                [INVENTORY]
                %s

                [EXPECTED MATERIAL DEMAND]
                %s

                [MISSION QUEUE]
                %s

                [AGV LOAD]
                %s

                [AI JUDGEMENT RULES]
                - Mission count alone is not a bottleneck.
                - One production request can generate multiple sequential missions.
                - IN_PROGRESS means the AGV is working normally.
                - Do not recommend interrupting an active mission flow.
                - If inventory available quantity is lower than expected demand, recommend replenishment priority.
                - AI only recommends. Scheduler and Dispatcher make the final control decision.
                """.formatted(
                buildInventorySummary(),
                buildExpectedDemandSummary(),
                buildMissionSummary(),
                buildAgvSummary()
        );
    }

    private String buildInventorySummary() {
        List<Inventory> inventories = inventoryRepository.findAll();

        if (inventories.isEmpty()) {
            return "- no inventory data";
        }

        return inventories.stream()
                .map(inventory -> {
                    int available =
                            inventory.getCurrentQuantity()
                                    - inventory.getReservedQuantity();

                    String risk =
                            available <= 0
                                    ? "SHORTAGE"
                                    : available <= inventory.getMinThreshold()
                                      ? "LOW"
                                      : "NORMAL";

                    return "- %s: current=%d, reserved=%d, available=%d, min=%d, status=%s, risk=%s"
                            .formatted(
                                    inventory.getMaterial().getMaterialCode(),
                                    inventory.getCurrentQuantity(),
                                    inventory.getReservedQuantity(),
                                    available,
                                    inventory.getMinThreshold(),
                                    inventory.getStatus(),
                                    risk
                            );
                })
                .toList()
                .toString();
    }

    private String buildExpectedDemandSummary() {
        List<ProductionTask> activeTasks =
                taskRepository.findByStatusIn(
                        List.of(
                                TaskStatus.READY,
                                TaskStatus.RUNNING
                        )
                );

        if (activeTasks.isEmpty()) {
            return "- no active production task";
        }

        Map<String, Integer> expectedDemandByMaterial =
                new LinkedHashMap<>();

        for (ProductionTask task : activeTasks) {
            List<ProductMaterial> bom =
                    productMaterialRepository.findByProduct_ProductType(
                            task.getProductType()
                    );

            for (ProductMaterial pm : bom) {
                Material material = pm.getMaterial();

                int required =
                        pm.getQuantityPerUnit()
                                * task.getQuantity();

                expectedDemandByMaterial.merge(
                        material.getMaterialCode(),
                        required,
                        Integer::sum
                );
            }
        }

        Map<String, Integer> availableByMaterial =
                new LinkedHashMap<>();

        for (Inventory inventory : inventoryRepository.findAll()) {
            int available =
                    inventory.getCurrentQuantity()
                            - inventory.getReservedQuantity();

            availableByMaterial.put(
                    inventory.getMaterial().getMaterialCode(),
                    available
            );
        }

        return expectedDemandByMaterial.entrySet()
                .stream()
                .map(entry -> {
                    String materialCode = entry.getKey();
                    int expectedDemand = entry.getValue();
                    int available =
                            availableByMaterial.getOrDefault(
                                    materialCode,
                                    0
                            );

                    boolean shortageExpected =
                            available < expectedDemand;

                    return "- %s: available=%d, expectedDemand=%d, shortageExpected=%s"
                            .formatted(
                                    materialCode,
                                    available,
                                    expectedDemand,
                                    shortageExpected
                            );
                })
                .toList()
                .toString();
    }

    private String buildMissionSummary() {
        List<Mission> missions =
                missionRepository.findByStatusNotOrderBySequenceOrderAsc(
                        MissionStatus.COMPLETED
                );

        if (missions.isEmpty()) {
            return "- no active mission";
        }

        LocalDateTime now =
                LocalDateTime.now();

        return missions.stream()
                .filter(mission -> mission.getStatus() != MissionStatus.CANCELLED)
                .limit(12)
                .map(mission -> {
                    long waitingSeconds =
                            secondsBetween(
                                    mission.getCreatedAt(),
                                    now
                            );

                    Long runningSeconds =
                            mission.getStartedAt() == null
                                    ? null
                                    : secondsBetween(
                                    mission.getStartedAt(),
                                    now
                            );

                    String agvText =
                            mission.getAgv() == null
                                    ? "unassigned"
                                    : "AGV%02d".formatted(
                                    mission.getAgv().getAgvId()
                            );

                    String materialText =
                            mission.getMaterial() == null
                                    ? "none"
                                    : mission.getMaterial().getMaterialCode();

                    String productText =
                            mission.getProduct() == null
                                    ? "none"
                                    : mission.getProduct().getProductType().name();

                    return "- missionId=%d, type=%s, status=%s, agv=%s, sequence=%d, material=%s, product=%s, waitingSeconds=%d, runningSeconds=%s"
                            .formatted(
                                    mission.getMissionId(),
                                    mission.getMissionType(),
                                    mission.getStatus(),
                                    agvText,
                                    mission.getSequenceOrder(),
                                    materialText,
                                    productText,
                                    waitingSeconds,
                                    runningSeconds == null
                                            ? "none"
                                            : runningSeconds.toString()
                            );
                })
                .toList()
                .toString();
    }

    private String buildAgvSummary() {
        List<Agv> agvs =
                agvRepository.findAll();

        if (agvs.isEmpty()) {
            return "- no agv data";
        }

        return agvs.stream()
                .map(agv -> {
                    long queueSize =
                            missionRepository.countByAgv_AgvIdAndStatusIn(
                                    agv.getAgvId(),
                                    List.of(
                                            MissionStatus.CREATED,
                                            MissionStatus.ASSIGNED,
                                            MissionStatus.IN_PROGRESS
                                    )
                            );

                    String currentMission =
                            agv.getCurrentMission() == null
                                    ? "none"
                                    : agv.getCurrentMission()
                                      .getMissionId()
                                      .toString();

                    return "- AGV%02d: role=%s, status=%s, currentMission=%s, activeQueueSize=%d"
                            .formatted(
                                    agv.getAgvId(),
                                    agv.getRole(),
                                    agv.getStatus(),
                                    currentMission,
                                    queueSize
                            );
                })
                .toList()
                .toString();
    }

    private long secondsBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (start == null || end == null) {
            return 0;
        }

        return Math.max(
                0,
                Duration.between(start, end).toSeconds()
        );
    }
}