package com.example.ssafy_pjt.backend.feature.agv.service;

import com.example.ssafy_pjt.backend.feature.agv.dto.AgvResponse;
import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.sender.AgvCommandSender;
import com.example.ssafy_pjt.backend.websocket.dto.CommandAssignMessage;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgvService {

    private final AgvRepository agvRepository;
    private final AgvCommandSender agvCommandSender;
    private final ArucoMarkerRepository arucoMarkerRepository;

    @Transactional(readOnly = true)
    public List<AgvResponse> getAgvs() {
        return agvRepository.findAll()
                .stream()
                .map(AgvResponse::new)
                .toList();
    }

    @Transactional
    public void markConnected(Integer agvId) {
        Agv agv = findAgv(agvId);

        if (agv.getStatus() == AgvStatus.OFFLINE) {
            agv.setStatus(AgvStatus.IDLE);
        }

        agv.setLastSeenAt(LocalDateTime.now());
    }

    @Transactional
    public void markDisconnected(Integer agvId) {
        Agv agv = findAgv(agvId);

        agv.setStatus(AgvStatus.OFFLINE);
        agv.setLastSeenAt(LocalDateTime.now());
    }

    @Transactional
    public void updateLastSeen(Integer agvId) {
        Agv agv = findAgv(agvId);

        agv.setLastSeenAt(LocalDateTime.now());
    }
    @Transactional
    public void updateStatus(AgvStatusMessage message) {

        Agv agv = agvRepository.findById(message.getAgvId())
                .orElseThrow();

        agv.setStatus(
                AgvStatus.valueOf(message.getStatus())
        );


        if (message.getLocated() != null) {

            ArucoMarker marker =
                    arucoMarkerRepository
                            .findById(message.getLocated())
                            .orElse(null);

            if (
                    marker.getXPosition() != null &&
                            marker.getYPosition() != null
            ) {
                agv.setCurrentMarker(marker);
            }
        }


        agv.setLastSeenAt(LocalDateTime.now());
    }

    @Transactional
    public void pauseAgv(Integer agvId) {

        Agv agv = findAgv(agvId);

        agv.setStatus(AgvStatus.STOP);
        agv.setLastSeenAt(LocalDateTime.now());

        agvCommandSender.sendCommand(
                agvId,
                CommandAssignMessage.builder()
                        .messageType("COMMAND_ASSIGN")
                        .agvId(agvId)
                        .taskId(null)
                        .commandId(null)
                        .command(MissionType.STOP)
                        .destination(null)
                        .cargo(null)
                        .build()
        );
    }

    @Transactional
    public void resumeAgv(Integer agvId) {

        Agv agv = findAgv(agvId);

        agv.setStatus(AgvStatus.MOVING);
        agv.setLastSeenAt(LocalDateTime.now());

        agvCommandSender.sendCommand(
                agvId,
                CommandAssignMessage.builder()
                        .messageType("COMMAND_ASSIGN")
                        .agvId(agvId)
                        .taskId(null)
                        .commandId(null)
                        .command(MissionType.RESUME)
                        .destination(null)
                        .cargo(null)
                        .build()
        );
    }

    @Transactional
    public void cancelCurrentTask(Integer agvId) {
        Agv agv = findAgv(agvId);

        agv.setCurrentMission(null);
        agv.setStatus(AgvStatus.IDLE);
        agv.setLastSeenAt(LocalDateTime.now());

        // TODO: 현재 Mission 취소 처리
        // TODO: AGV에게 STOP/CANCEL 명령 전송
    }

    private Agv findAgv(Integer agvId) {
        return agvRepository.findById(agvId)
                .orElseThrow(() ->
                        new IllegalArgumentException("AGV를 찾을 수 없습니다. agvId=" + agvId)
                );
    }
}