package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.event.repository.EventLogRepository;
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

    // 추가
    private final EventLogRepository eventLogRepository;


    public String buildSummary() {

        return """
                [FACTORY STATE]

                [INVENTORY]
                %s

                [EXPECTED MATERIAL DEMAND]
                %s

                [MISSION QUEUE]
                %s

                [AGV STATUS]
                %s

                [RECENT EVENT HISTORY]
                %s

                [AI JUDGEMENT RULES]
                - Mission count alone is not a bottleneck.
                - One production request can generate multiple sequential missions.
                - IN_PROGRESS means the AGV is working normally.
                - Do not recommend interrupting an active mission flow.

                - Analyze event history together with current state.
                - Repeated WARNING or ERROR events may indicate abnormal operation.
                - Consider how long an AGV has remained in the same state.
                - Explain the reason, not only the status.

                - AI only recommends.
                - Scheduler and Dispatcher make the final control decision.
                """.formatted(
                buildInventorySummary(),
                buildExpectedDemandSummary(),
                buildMissionSummary(),
                buildAgvSummary(),
                buildEventSummary()
        );
    }


    private String buildEventSummary() {

        var events =
                eventLogRepository
                        .findTop50ByOrderByCreatedAtDesc();

        if (events.isEmpty()) {
            return "- no recent events";
        }

        return events.stream()
                .limit(20)
                .map(event ->
                        "- time=%s, type=%s, level=%s, target=%s:%s, message=%s"
                                .formatted(
                                        event.getCreatedAt(),
                                        event.getEventType(),
                                        event.getLevel(),
                                        event.getTargetType(),
                                        event.getTargetId(),
                                        event.getMessage()
                                )
                )
                .toList()
                .toString();
    }


    private String buildInventorySummary() {

        List<Inventory> inventories =
                inventoryRepository.findAll();

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

                    return "- %s available=%d risk=%s"
                            .formatted(
                                    inventory.getMaterial().getMaterialCode(),
                                    available,
                                    risk
                            );
                })
                .toList()
                .toString();
    }


    private String buildExpectedDemandSummary() {

        List<ProductionTask> tasks =
                taskRepository.findByStatusIn(
                        List.of(
                                TaskStatus.READY,
                                TaskStatus.RUNNING
                        )
                );

        if (tasks.isEmpty()) {
            return "- no active production task";
        }


        Map<String, Integer> demand =
                new LinkedHashMap<>();

        for (ProductionTask task : tasks) {

            List<ProductMaterial> bom =
                    productMaterialRepository
                            .findByProduct_ProductType(
                                    task.getProductType()
                            );

            for (ProductMaterial pm : bom) {

                Material material =
                        pm.getMaterial();

                demand.merge(
                        material.getMaterialCode(),
                        pm.getQuantityPerUnit()
                                * task.getQuantity(),
                        Integer::sum
                );
            }
        }

        return demand.toString();
    }


    private String buildMissionSummary() {

        List<Mission> missions =
                missionRepository
                        .findByStatusNotOrderBySequenceOrderAsc(
                                MissionStatus.COMPLETED
                        );

        if (missions.isEmpty()) {
            return "- no active mission";
        }

        LocalDateTime now =
                LocalDateTime.now();

        return missions.stream()
                .limit(12)
                .map(mission -> {

                    long wait =
                            secondsBetween(
                                    mission.getCreatedAt(),
                                    now
                            );

                    return "- mission=%d type=%s status=%s waiting=%ds"
                            .formatted(
                                    mission.getMissionId(),
                                    mission.getMissionType(),
                                    mission.getStatus(),
                                    wait
                            );
                })
                .toList()
                .toString();
    }


    private String buildAgvSummary() {

        List<Agv> agvs =
                agvRepository.findAll();

        return agvs.stream()
                .map(agv ->
                        "- AGV%02d role=%s status=%s mission=%s"
                                .formatted(
                                        agv.getAgvId(),
                                        agv.getRole(),
                                        agv.getStatus(),
                                        agv.getCurrentMission() == null
                                                ? "none"
                                                : agv.getCurrentMission()
                                                  .getMissionId()
                                )
                )
                .toList()
                .toString();
    }


    private long secondsBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (start == null) {
            return 0;
        }

        return Math.max(
                0,
                Duration.between(
                        start,
                        end
                ).toSeconds()
        );
    }
}