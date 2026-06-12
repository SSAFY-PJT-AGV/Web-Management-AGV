package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionPriorityService {

    private final InventoryRepository inventoryRepository;

    public int calculateScore(Mission mission, Agv agv) {
        int score = 0;

        score += getMissionTypeScore(mission);
        score += getTaskPriorityScore(mission);
        score += getInventoryScore(mission);
        score += getWaitingTimeScore(mission);
        score += getRoleFitScore(mission, agv);

        return score;
    }

    private int getTaskPriorityScore(Mission mission) {
        if (mission.getTask() == null || mission.getTask().getPriority() == null) {
            return 0;
        }

        TaskPriority priority = mission.getTask().getPriority();

        return switch (priority) {
            case LOW -> 10;
            case NORMAL -> 30;
            case HIGH -> 60;
        };
    }

    private int getInventoryScore(Mission mission) {
        if (mission.getMaterial() == null) {
            return 0;
        }

        String materialCode = mission.getMaterial().getMaterialCode();

        return inventoryRepository.findByMaterial_MaterialCode(materialCode)
                .map(this::toInventoryScore)
                .orElse(0);
    }

    private int toInventoryScore(Inventory inventory) {
        int current = inventory.getCurrentQuantity() == null ? 0 : inventory.getCurrentQuantity();
        int reserved = inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
        int min = inventory.getMinThreshold() == null ? 0 : inventory.getMinThreshold();

        int available = current - reserved;

        if (available <= 0) {
            return 100;
        }

        if (available <= min) {
            return 60;
        }

        InventoryStatus status = inventory.getStatus();

        return switch (status) {
            case SHORTAGE -> 80;
            case LOW -> 40;
            case NORMAL -> 0;
        };
    }

    private int getRoleFitScore(Mission mission, Agv agv) {
        if (agv.getRole() == null) {
            return 0;
        }

        AgvRole requiredRole = getRequiredRole(mission);

        if (agv.getRole() == requiredRole) {
            return 50;
        }

        if (agv.getRole() == AgvRole.BOTH) {
            return 30;
        }

        return -100;
    }

    private AgvRole getRequiredRole(Mission mission) {
        MissionType type = mission.getMissionType();

        return switch (type) {
            case PICK_FROM_STORAGE,
                 DROP_TO_CONVEYOR,
                 PICK_EMPTY_BOX,
                 PICK_FROM_CROSS,
                 DROP_TO_STORAGE,
                 RETURN_TO_BASE -> AgvRole.SUPPLY;

            case PICK_FROM_CONVEYOR,
                 DROP_TO_FINISHED_BOX_STORAGE,
                 PICK_FROM_INBOUND,
                 DROP_TO_OUTBOUND,
                 DROP_EMPTY_BOX -> AgvRole.COLLECT;

            case DROP_TO_CROSS -> mission.getMaterial() == null
                    ? AgvRole.SUPPLY
                    : AgvRole.COLLECT;

            case WAIT,
                 STOP,
                 RESUME -> AgvRole.BOTH;
        };
    }

    private int getMissionTypeScore(Mission mission) {
        MissionType type = mission.getMissionType();

        return switch (type) {
            case PICK_FROM_CONVEYOR -> 100;
            case DROP_TO_FINISHED_BOX_STORAGE -> 90;
            case PICK_FROM_INBOUND, DROP_TO_OUTBOUND -> 80;
            case PICK_FROM_STORAGE, DROP_TO_CONVEYOR -> 60;
            case PICK_EMPTY_BOX, DROP_EMPTY_BOX -> 50;
            case PICK_FROM_CROSS, DROP_TO_CROSS -> 40;
            case DROP_TO_STORAGE -> 30;
            case RETURN_TO_BASE -> 10;
            case WAIT, STOP, RESUME -> 0;
        };
    }

    private int getWaitingTimeScore(Mission mission) {
        if (mission.getCreatedAt() == null) {
            return 0;
        }

        long waitingMinutes = Duration.between(
                mission.getCreatedAt(),
                LocalDateTime.now()
        ).toMinutes();

        return (int) Math.min(waitingMinutes * 2, 50);
    }
}