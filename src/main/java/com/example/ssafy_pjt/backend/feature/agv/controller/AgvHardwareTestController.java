package com.example.ssafy_pjt.backend.feature.agv.controller;

import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.websocket.dto.CommandAssignMessage;
import com.example.ssafy_pjt.backend.websocket.sender.AgvCommandSender;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/test/hardware")
@RequiredArgsConstructor
public class AgvHardwareTestController {

    private final AgvCommandSender agvCommandSender;

    private final AtomicLong mockCommandId = new AtomicLong(1000L);

    // AGV01

    @PostMapping("/agv/1/pick-from-storage")
    public String agv01PickFromStorage() {
        send(1, 1L, MissionType.PICK_FROM_STORAGE, 101, 3);
        return "AGV01 PICK_FROM_STORAGE 전송 완료";
    }

    @PostMapping("/agv/1/drop-to-conveyor")
    public String agv01DropToConveyor() {
        send(1, 1L, MissionType.DROP_TO_CONVEYOR, 201, 3);
        return "AGV01 DROP_TO_CONVEYOR 전송 완료";
    }

    @PostMapping("/agv/1/pick-empty-box")
    public String agv01PickEmptyBox() {
        send(1, 1L, MissionType.PICK_EMPTY_BOX, 301, null);
        return "AGV01 PICK_EMPTY_BOX 전송 완료";
    }

    @PostMapping("/agv/1/drop-to-cross")
    public String agv01DropToCross() {
        send(1, 1L, MissionType.DROP_TO_CROSS, 401, null);
        return "AGV01 DROP_TO_CROSS 전송 완료";
    }

    @PostMapping("/agv/1/pick-from-cross")
    public String agv01PickFromCross() {
        send(1, 1L, MissionType.PICK_FROM_CROSS, 401, null);
        return "AGV01 PICK_FROM_CROSS 전송 완료";
    }

    @PostMapping("/agv/1/drop-to-storage")
    public String agv01DropToStorage() {
        send(1, 1L, MissionType.DROP_TO_STORAGE, 102, null);
        return "AGV01 DROP_TO_STORAGE 전송 완료";
    }

    // AGV02

    @PostMapping("/agv/2/pick-from-conveyor")
    public String agv02PickFromConveyor() {
        send(2, 2L, MissionType.PICK_FROM_CONVEYOR, 202, 3);
        return "AGV02 PICK_FROM_CONVEYOR 전송 완료";
    }

    @PostMapping("/agv/2/drop-to-finished-box-storage")
    public String agv02DropToFinishedBoxStorage() {
        send(2, 2L, MissionType.DROP_TO_FINISHED_BOX_STORAGE, 501, 3);
        return "AGV02 DROP_TO_FINISHED_BOX_STORAGE 전송 완료";
    }

    @PostMapping("/agv/2/pick-from-inbound")
    public String agv02PickFromInbound() {
        send(2, 2L, MissionType.PICK_FROM_INBOUND, 601, null);
        return "AGV02 PICK_FROM_INBOUND 전송 완료";
    }

    @PostMapping("/agv/2/drop-to-cross")
    public String agv02DropToCross() {
        send(2, 2L, MissionType.DROP_TO_CROSS, 401, null);
        return "AGV02 DROP_TO_CROSS 전송 완료";
    }

    @PostMapping("/agv/2/pick-empty-box")
    public String agv02PickEmptyBox() {
        send(2, 2L, MissionType.PICK_EMPTY_BOX, 502, null);
        return "AGV02 PICK_EMPTY_BOX 전송 완료";
    }

    @PostMapping("/agv/2/drop-empty-box")
    public String agv02DropEmptyBox() {
        send(2, 2L, MissionType.DROP_EMPTY_BOX, 602, null);
        return "AGV02 DROP_EMPTY_BOX 전송 완료";
    }

    // 제어 명령

    @PostMapping("/agv/{agvId}/wait")
    public String waitAgv(@PathVariable Integer agvId) {
        send(agvId, null, MissionType.WAIT, null, null);
        return "AGV" + agvId + " WAIT 전송 완료";
    }

    @PostMapping("/agv/{agvId}/stop")
    public String stopAgv(@PathVariable Integer agvId) {
        send(agvId, null, MissionType.STOP, null, null);
        return "AGV" + agvId + " STOP 전송 완료";
    }

    @PostMapping("/agv/{agvId}/resume")
    public String resumeAgv(@PathVariable Integer agvId) {
        send(agvId, null, MissionType.RESUME, null, null);
        return "AGV" + agvId + " RESUME 전송 완료";
    }

    // 재고 부족 / 교차 구역 테스트

    @PostMapping("/replenishment/start")
    public String startReplenishmentMission() {
        send(1, 999L, MissionType.DROP_TO_CROSS, 401, null);
        send(2, 999L, MissionType.PICK_FROM_CROSS, 401, null);

        return "재고부족/교차구역 테스트 미션 전송 완료";
    }

    private void send(
            Integer agvId,
            Long taskId,
            MissionType command,
            Integer destination,
            Integer cargo
    ) {
        CommandAssignMessage message = CommandAssignMessage.builder()
                .messageType("COMMAND_ASSIGN")
                .agvId(agvId)
                .taskId(taskId)
                .commandId(mockCommandId.getAndIncrement())
                .command(command)
                .destination(destination)
                .cargo(cargo)
                .build();

        agvCommandSender.sendCommand(agvId, message);
    }
}