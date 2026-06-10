package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MissionPriorityService {

    public int calculateScore(Mission mission, Agv agv) {
        int score = 0;

        score += getMissionTypeScore(mission);
        score += getWaitingTimeScore(mission);

        if (agv.getRole() == AgvRole.COLLECT) {
            score += getAgv2BonusScore(mission);
        }

        return score;
    }

    private int getMissionTypeScore(Mission mission) {
        MissionType type = mission.getMissionType();

        return switch (type) {
            case PICK_FROM_CONVEYOR -> 100;
            case DROP_TO_FINISHED_BOX_STORAGE -> 90;

            case PICK_FROM_INBOUND,
                 DROP_TO_OUTBOUND -> 80;

            case PICK_FROM_STORAGE,
                 DROP_TO_CONVEYOR -> 60;

            case PICK_EMPTY_BOX,
                 DROP_EMPTY_BOX -> 50;

            case PICK_FROM_CROSS,
                 DROP_TO_CROSS -> 40;

            case DROP_TO_STORAGE -> 30;
            case RETURN_TO_BASE -> 10;

            case WAIT,
                 STOP,
                 RESUME -> 0;
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

    private int getAgv2BonusScore(Mission mission) {
        MissionType type = mission.getMissionType();

        return switch (type) {
            case PICK_FROM_CONVEYOR,
                 DROP_TO_FINISHED_BOX_STORAGE,
                 PICK_FROM_INBOUND,
                 DROP_TO_OUTBOUND,
                 DROP_EMPTY_BOX -> 30;

            default -> 0;
        };
    }
}