package com.example.ssafy_pjt.backend.websocket.dto;

import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandAssignMessage {

    private String messageType;
    private String agvId;
    private String taskId;
    private String commandId;

    private MissionType command;

    private Integer destination;
    private String cargo;
}