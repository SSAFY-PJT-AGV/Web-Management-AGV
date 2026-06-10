package com.example.ssafy_pjt.backend.feature.agv.service;

import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.dto.CommandAssignMessage;
import com.example.ssafy_pjt.backend.websocket.sender.AgvCommandSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ChipScenarioTestService {

    private final AgvCommandSender agvCommandSender;

    private final AtomicLong commandIdGenerator = new AtomicLong(1000L);

    private final Map<Integer, Queue<CommandAssignMessage>> queues =
            new ConcurrentHashMap<>();

    private final Map<Long, Integer> commandOwner =
            new ConcurrentHashMap<>();

    private boolean running = false;

    public void startScenario() {
        resetScenario();

        running = true;

        Queue<CommandAssignMessage> agv1Queue = new LinkedList<>();
        Queue<CommandAssignMessage> agv2Queue = new LinkedList<>();

        agv1Queue.add(command(
                1,
                1L,
                MissionType.PICK_FROM_STORAGE,
                3,
                "CHIP"
        ));

        agv1Queue.add(command(
                1,
                1L,
                MissionType.DROP_TO_CONVEYOR,
                1,
                "CHIP"
        ));

        agv2Queue.add(command(
                2,
                1L,
                MissionType.PICK_FROM_CONVEYOR,
                1,
                "CHIP"
        ));

        agv2Queue.add(command(
                2,
                1L,
                MissionType.DROP_TO_FINISHED_BOX_STORAGE,
                11,
                "CHIP"
        ));

        queues.put(1, agv1Queue);
        queues.put(2, agv2Queue);

        dispatchNext(1);
    }

    public void resetScenario() {
        queues.clear();
        commandOwner.clear();
        running = false;
    }

    public void handleAgvDone(AgvStatusMessage message) {
        if (!running) {
            return;
        }

        if (!"DONE".equals(message.getEvent())) {
            return;
        }

        Integer agvId = message.getAgvId();
        Long completedCommandId = message.getCommandId();

        System.out.println("[SCENARIO DONE] agvId=" + agvId
                + ", commandId=" + completedCommandId);

        if (agvId == 1) {
            dispatchNext(1);

            if (isQueueEmpty(1)) {
                dispatchNext(2);
            }

            return;
        }

        if (agvId == 2) {
            dispatchNext(2);

            if (isQueueEmpty(1) && isQueueEmpty(2)) {
                running = false;
                System.out.println("[SCENARIO COMPLETE] CHIP → CAR_CONTROL_UNIT 완료");
            }
        }
    }

    private void dispatchNext(Integer agvId) {
        Queue<CommandAssignMessage> queue = queues.get(agvId);

        if (queue == null || queue.isEmpty()) {
            return;
        }

        CommandAssignMessage command = queue.poll();

        commandOwner.put(command.getCommandId(), agvId);

        agvCommandSender.sendCommand(agvId, command);

        System.out.println("[SCENARIO SEND] agvId=" + agvId
                + ", command=" + command.getCommand()
                + ", destination=" + command.getDestination()
                + ", cargo=" + command.getCargo());
    }

    private boolean isQueueEmpty(Integer agvId) {
        Queue<CommandAssignMessage> queue = queues.get(agvId);
        return queue == null || queue.isEmpty();
    }

    private CommandAssignMessage command(
            Integer agvId,
            Long taskId,
            MissionType command,
            Integer destination,
            String cargo
    ) {
        return CommandAssignMessage.builder()
                .messageType("COMMAND_ASSIGN")
                .agvId(agvId)
                .taskId(taskId)
                .commandId(commandIdGenerator.getAndIncrement())
                .command(command)
                .destination(destination)
                .cargo(cargo)
                .build();
    }

    public boolean isRunning() {
        return running;
    }
}