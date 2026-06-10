package com.example.ssafy_pjt.backend.websocket.dto;

import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandAssignMessage {

    private String messageType;

    private Integer agvId;

    private Long taskId;

    // commandId = missionId
    private Long commandId;

    private MissionType command;

    private Integer destination;

    private String cargo;
}