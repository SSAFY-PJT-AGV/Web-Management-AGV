package com.example.ssafy_pjt.backend.feature.agv.controller;

import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.websocket.dto.CommandAssignMessage;
import com.example.ssafy_pjt.backend.websocket.sender.AgvCommandSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/agv")
@RequiredArgsConstructor
public class AgvTestController {

    private final AgvCommandSender agvCommandSender;

    @PostMapping("/{agvId}/command")
    public String sendCommand(
            @PathVariable Integer agvId,
            @RequestBody TestCommandRequest request
    ) {
        CommandAssignMessage message = CommandAssignMessage.builder()
                .messageType("COMMAND_ASSIGN")
                .commandId(Long.valueOf(request.getCommandId()))
                .taskId(Long.valueOf(request.getTaskId()))
                .command(MissionType.valueOf(request.getCommand()))
                .destination(request.getDestination())
                .cargo(request.getCargo())
                .build();

        agvCommandSender.sendCommand(agvId, message);

        return "COMMAND_ASSIGN 전송 완료";
    }

    @Getter
    @Setter
    public static class TestCommandRequest {
        private String commandId;
        private String taskId;
        private String command;
        private Integer destination;
        private String cargo;
    }
}