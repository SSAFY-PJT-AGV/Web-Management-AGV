package com.example.ssafy_pjt.backend.feature.agv.dto;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AgvResponse {

    private final Integer agvId;
    private final String status;
    private final Integer currentMarkerId;
    private final Long currentMissionId;
    private final String cargoType;
    private final String cargoMaterialCode;
    private final String targetZoneName;
    private final LocalDateTime lastSeenAt;
    private final String currentMissionType;


    public AgvResponse(Agv agv) {
        this.agvId = agv.getAgvId();

        this.status = agv.getStatus().name();

        this.currentMarkerId = agv.getCurrentMarker() == null
                ? null
                : agv.getCurrentMarker().getMarkerId();

        this.currentMissionId = agv.getCurrentMission() == null
                ? null
                : agv.getCurrentMission().getMissionId();

        this.cargoType = agv.getCargoType() == null
                ? null
                : agv.getCargoType().name();

        this.cargoMaterialCode = agv.getCargoMaterial() == null
                ? null
                : agv.getCargoMaterial().getMaterialCode();

        this.targetZoneName =
                agv.getCurrentMission() == null
                        || agv.getCurrentMission().getTargetZone() == null
                        ? null
                        : agv.getCurrentMission().getTargetZone().getZoneName();

        this.lastSeenAt = agv.getLastSeenAt();

        this.currentMissionType =
                agv.getCurrentMission() == null
                        ? null
                        : agv.getCurrentMission()
                          .getMissionType()
                          .name();
    }
}