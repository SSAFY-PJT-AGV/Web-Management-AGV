package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.reservation.service.ReservationService;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionResultService {

    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;
    private final MissionDispatchService missionDispatchService;
    private final ReservationService reservationService;
    private final DashboardBroadcastService dashboardBroadcastService;

    @Transactional
    public void handleAgvDone(AgvStatusMessage message) {
        Mission mission = missionRepository.findById(message.getCommandId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mission 없음 commandId=" + message.getCommandId()
                ));

        Agv agv = agvRepository.findById(message.getAgvId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "AGV 없음 agvId=" + message.getAgvId()
                ));

        handleSuccess(mission, agv);
    }

    private void handleSuccess(Mission mission, Agv agv) {
        mission.setStatus(MissionStatus.COMPLETED);
        mission.setCompletedAt(LocalDateTime.now());

        updateReservationByCompletedMission(mission, agv);

        agv.setStatus(AgvStatus.IDLE);
        agv.setCurrentMission(null);

        System.out.println(
                "[MISSION COMPLETE] mission="
                        + mission.getMissionId()
                        + ", agv="
                        + agv.getAgvId()
        );

        dashboardBroadcastService.missionRefresh();
        dashboardBroadcastService.mapRefresh();

        missionDispatchService.dispatchAvailableAgvs();
    }

    private void updateReservationByCompletedMission(Mission mission, Agv agv) {
        MissionType type = mission.getMissionType();

        if (type == MissionType.DROP_TO_CROSS) {
            reservationService.occupyCrossZone(agv, mission);
            return;
        }

        if (type == MissionType.PICK_FROM_CROSS
                || type == MissionType.DROP_TO_STORAGE) {
            reservationService.releaseCrossZone(agv, mission);
        }
    }
}