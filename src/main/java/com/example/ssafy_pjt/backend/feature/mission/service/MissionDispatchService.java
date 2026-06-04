package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionDispatchService {

    private static final Integer AGV01_ID = 1;
    private static final Integer AGV02_ID = 2;

    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;

    @Transactional
    public Mission dispatchNextMission(Integer agvId) {
        Agv agv = agvRepository.findById(agvId)
                .orElseThrow(() -> new IllegalArgumentException("AGV " + agvId + "번이 존재하지 않습니다."));

        // IDLE일 때만 새 Mission 1개 배정
        if (agv.getStatus() != AgvStatus.IDLE) {
            return null;
        }

        Mission mission = findNextMissionForAgv(agvId);

        if (mission == null) {
            return null;
        }

        if (shouldWait(mission)) {
            mission.setAgv(agv);
            mission.setStatus(MissionStatus.ASSIGNED);

            agv.setStatus(AgvStatus.WAITING);
            agv.setCurrentMission(mission);

            return mission;
        }

        mission.setAgv(agv);
        mission.setStatus(MissionStatus.ASSIGNED);

        agv.setStatus(AgvStatus.ASSIGNED);
        agv.setCurrentMission(mission);

        // TODO: WebSocket COMMAND_ASSIGN 전송

        return mission;
    }

    private Mission findNextMissionForAgv(Integer agvId) {
        List<Mission> createdMissions =
                missionRepository.findByStatusOrderByCreatedAtAsc(MissionStatus.CREATED);

        return createdMissions.stream()
                .filter(mission -> isMissionForAgv(agvId, mission))
                .findFirst()
                .orElse(null);
    }

    private boolean isMissionForAgv(Integer agvId, Mission mission) {
        if (AGV01_ID.equals(agvId)) {
            return isAgv01Mission(mission);
        }

        if (AGV02_ID.equals(agvId)) {
            return isAgv02Mission(mission);
        }

        throw new IllegalArgumentException("지원하지 않는 AGV ID입니다: " + agvId);
    }

    private boolean isAgv01Mission(Mission mission) {
        MissionType type = mission.getMissionType();

        return type == MissionType.PICK_FROM_STORAGE
                || type == MissionType.DROP_TO_CONVEYOR
                || type == MissionType.PICK_EMPTY_BOX
                || type == MissionType.PICK_FROM_CROSS
                || type == MissionType.DROP_TO_STORAGE
                || type == MissionType.RETURN_TO_BASE

                // AGV01: 빈 상자를 교차구역에 내려놓음
                || (type == MissionType.DROP_TO_CROSS
                && mission.getMaterial() == null);
    }

    private boolean isAgv02Mission(Mission mission) {
        MissionType type = mission.getMissionType();

        return type == MissionType.PICK_FROM_CONVEYOR
                || type == MissionType.DROP_TO_FINISHED_BOX_STORAGE
                || type == MissionType.PICK_FROM_INBOUND
                || type == MissionType.DROP_TO_OUTBOUND
                || type == MissionType.DROP_EMPTY_BOX

                // AGV02: 보급 자재 상자를 교차구역에 내려놓음
                || (type == MissionType.DROP_TO_CROSS
                && mission.getMaterial() != null);
    }

    private boolean shouldWait(Mission mission) {
        return isAgv01CrossMission(mission)
                && shouldAgv01WaitForAgv02();
    }

    private boolean isAgv01CrossMission(Mission mission) {
        MissionType type = mission.getMissionType();

        return (type == MissionType.DROP_TO_CROSS && mission.getMaterial() == null)
                || type == MissionType.PICK_FROM_CROSS
                || type == MissionType.DROP_TO_STORAGE;
    }

    private boolean shouldAgv01WaitForAgv02() {
        return missionRepository.existsByMissionTypeAndStatusIn(
                MissionType.DROP_TO_CROSS,
                List.of(
                        MissionStatus.CREATED,
                        MissionStatus.ASSIGNED,
                        MissionStatus.IN_PROGRESS
                )
        );
    }
}