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

    // 어떤 AGV에게 내린 명령인지
    private Integer agvId;

    // 생산 작업 ID (DB task_id)
    private Long taskId;

    // 명령 추적 ID (DB mission_id)
    private Long commandId;

    // 수행 명령
    private MissionType command;

    // 목표 ArUco marker ID
    private Integer destination;

    private Integer cargo;
}