package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.marker.service.MarkerResolveService;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.reservation.service.ReservationService;
import com.example.ssafy_pjt.backend.websocket.dto.CommandAssignMessage;
import com.example.ssafy_pjt.backend.websocket.sender.AgvCommandSender;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import com.example.ssafy_pjt.backend.websocket.session.AgvSessionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionDispatchService {

    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;
    private final AgvCommandSender agvCommandSender;
    private final ReservationService reservationService;
    private final MarkerResolveService markerResolveService;
    private final MissionPriorityService missionPriorityService;
    private final AgvSessionHandler agvSessionHandler;
    private final DashboardBroadcastService dashboardBroadcastService;

    @Transactional
    public void assignCreatedMissionsToAgvQueues() {
        List<Mission> createdMissions =
                missionRepository.findByStatusOrderByCreatedAtAsc(MissionStatus.CREATED);

        for (Mission mission : createdMissions) {
            Agv selectedAgv = selectBestAgvForMission(mission);

            mission.setAgv(selectedAgv);
            mission.setStatus(MissionStatus.ASSIGNED);
        }

        dashboardBroadcastService.missionRefresh();
    }

    @Transactional
    public void dispatchAvailableAgvs() {
        List<Agv> availableAgvs = agvRepository.findByStatusIn(
                List.of(AgvStatus.IDLE, AgvStatus.WAITING)
        );

        for (Agv agv : availableAgvs) {
            dispatchNextMission(agv.getAgvId());
        }
    }

    @Transactional
    public Mission dispatchNextMission(Integer agvId) {
        Agv agv = getAgv(agvId);

        if (!isDispatchable(agv)) {
            return null;
        }

        Mission mission = findNextAssignedMissionForAgv(agvId);

        if (mission == null) {
            return null;
        }

        if (shouldWait(mission)) {
            agv.setStatus(AgvStatus.WAITING);
            agv.setCurrentMission(mission);

            dashboardBroadcastService.missionRefresh();
            dashboardBroadcastService.mapRefresh();

            return mission;
        }

        if (isCrossMission(mission)) {
            reservationService.reserveCrossZone(agv, mission);
        }

        mission.setStatus(MissionStatus.IN_PROGRESS);

        agv.setStatus(AgvStatus.ASSIGNED);
        agv.setCurrentMission(mission);

        boolean sent = sendCommandAssign(agvId, mission);

        if (!sent) {
            mission.setStatus(MissionStatus.ASSIGNED);
            agv.setStatus(AgvStatus.IDLE);
            agv.setCurrentMission(null);

            dashboardBroadcastService.missionRefresh();
            dashboardBroadcastService.mapRefresh();

            return null;
        }

        dashboardBroadcastService.missionRefresh();
        dashboardBroadcastService.mapRefresh();

        return mission;
    }

    private boolean isDispatchable(Agv agv) {
        if (!agvSessionHandler.isConnected(agv.getAgvId())) {
            System.out.println("[DISPATCH SKIP] AGV not connected. agvId=" + agv.getAgvId());
            return false;
        }

        return agv.getStatus() == AgvStatus.IDLE
                || agv.getStatus() == AgvStatus.WAITING;
    }

    private Agv selectBestAgvForMission(Mission mission) {
        AgvRole requiredRole = getRequiredRole(mission);

        List<Agv> candidates = findCandidateAgvs(requiredRole);

        if (candidates.isEmpty()) {
            throw new IllegalStateException("배정 가능한 AGV가 없습니다. requiredRole=" + requiredRole);
        }

        return candidates.stream()
                .min(Comparator.comparingLong(this::getQueueSize))
                .orElseThrow();
    }

    private List<Agv> findCandidateAgvs(AgvRole requiredRole) {
        if (requiredRole == AgvRole.SUPPLY) {
            return agvRepository.findByRoleIn(List.of(AgvRole.SUPPLY, AgvRole.BOTH));
        }

        if (requiredRole == AgvRole.COLLECT) {
            return agvRepository.findByRoleIn(List.of(AgvRole.COLLECT, AgvRole.BOTH));
        }

        return agvRepository.findByRoleIn(List.of(AgvRole.BOTH));
    }

    private long getQueueSize(Agv agv) {
        return missionRepository.countByAgv_AgvIdAndStatusIn(
                agv.getAgvId(),
                List.of(MissionStatus.ASSIGNED, MissionStatus.IN_PROGRESS)
        );
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

    private Mission findNextAssignedMissionForAgv(Integer agvId) {
        Agv agv = getAgv(agvId);

        return missionRepository.findByAgv_AgvIdAndStatusOrderBySequenceOrderAsc(
                        agvId,
                        MissionStatus.ASSIGNED
                )
                .stream()
                .max(Comparator
                        .comparingInt((Mission mission) ->
                                missionPriorityService.calculateScore(mission, agv)
                        )
                        .thenComparing(
                                Mission::getSequenceOrder,
                                Comparator.reverseOrder()
                        )
                )
                .orElse(null);
    }

    private boolean shouldWait(Mission mission) {
        if (!isCrossMission(mission)) {
            return false;
        }

        return !reservationService.isCrossZoneAvailable();
    }

    private boolean isCrossMission(Mission mission) {
        MissionType type = mission.getMissionType();

        return type == MissionType.DROP_TO_CROSS
                || type == MissionType.PICK_FROM_CROSS
                || type == MissionType.DROP_TO_STORAGE;
    }

    private boolean sendCommandAssign(Integer agvId, Mission mission) {
        Integer destination = markerResolveService.resolveDestinationMarkerId(mission);

        CommandAssignMessage message = CommandAssignMessage.builder()
                .messageType("COMMAND_ASSIGN")
                .agvId(agvId)
                .taskId(mission.getTask() == null ? null : mission.getTask().getTaskId())
                .commandId(mission.getMissionId())
                .command(mission.getMissionType())
                .destination(destination)
                .cargo(
                        mission.getMaterial() == null
                                ? null
                                : mission.getMaterial().getMaterialCode()
                )
                .build();

        try {
            agvCommandSender.sendCommand(agvId, message);
            return true;
        } catch (Exception e) {
            System.out.println("[COMMAND_ASSIGN SEND FAIL] agvId=" + agvId + ", reason=" + e.getMessage());
            return false;
        }
    }

    private Agv getAgv(Integer agvId) {
        return agvRepository.findById(agvId)
                .orElseThrow(() -> new IllegalArgumentException("AGV " + agvId + "번이 존재하지 않습니다."));
    }
}