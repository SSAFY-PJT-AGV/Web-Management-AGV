package com.example.ssafy_pjt.backend.feature.mission.dto;

import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MissionResponse {

    private final Long missionId;
    private final Long taskId;
    private final Integer agvId;
    private final String missionType;
    private final String materialCode;
    private final String status;
    private final Integer sequenceOrder;
    private final Integer quantity;
    private final String sourceZone;
    private final String targetZone;
    private final LocalDateTime createdAt;

    public MissionResponse(Mission mission) {
        this.missionId = mission.getMissionId();

        this.taskId = mission.getTask() == null
                ? null
                : mission.getTask().getTaskId();

        this.agvId = mission.getAgv() == null
                ? null
                : mission.getAgv().getAgvId();

        this.missionType = mission.getMissionType().name();

        this.materialCode = mission.getMaterial() == null
                ? null
                : mission.getMaterial().getMaterialCode();

        this.status = mission.getStatus().name();
        this.sequenceOrder = mission.getSequenceOrder();
        this.quantity = mission.getQuantity();

        this.sourceZone = mission.getSourceZone() == null
                ? null
                : mission.getSourceZone().getZoneName();

        this.targetZone = mission.getTargetZone() == null
                ? null
                : mission.getTargetZone().getZoneName();

        this.createdAt = mission.getCreatedAt();
    }
}