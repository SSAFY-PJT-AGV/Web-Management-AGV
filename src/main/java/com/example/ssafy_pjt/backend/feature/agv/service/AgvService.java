package com.example.ssafy_pjt.backend.feature.agv.service;

import com.example.ssafy_pjt.backend.feature.agv.dto.AgvResponse;
import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.enums.CargoType;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.material.repository.MaterialRepository;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgvService {

    private final AgvRepository agvRepository;
    private final ArucoMarkerRepository arucoMarkerRepository;
    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public List<AgvResponse> getAgvs() {
        return agvRepository.findAllWithDisplayInfo()
                .stream()
                .map(AgvResponse::new)
                .toList();
    }

    @Transactional
    public void markConnected(Integer agvId) {
        Agv agv = findAgv(agvId);

        agv.setStatus(AgvStatus.IDLE);
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

        agv.setStatus(AgvStatus.valueOf(message.getStatus()));
        agv.setLastSeenAt(LocalDateTime.now());

        if (message.getLocated() != null) {
            arucoMarkerRepository.findById(message.getLocated())
                    .ifPresent(agv::setCurrentMarker);
        }

        String cargo = message.getCargo();

        if (cargo == null || cargo.isBlank()
                || "EMPTY".equals(cargo)
                || "NONE".equals(cargo)) {
            agv.setCargoType(CargoType.NONE);
            agv.setCargoMaterial(null);
            return;
        }

        materialRepository.findByMaterialCode(cargo)
                .ifPresent(material -> {
                    agv.setCargoType(CargoType.MATERIAL);
                    agv.setCargoMaterial(material);
                });
    }

    @Transactional
    public void pauseAgv(Integer agvId) {
        Agv agv = findAgv(agvId);

        agv.setStatus(AgvStatus.WAITING);
        agv.setLastSeenAt(LocalDateTime.now());

        // TODO: WebSocket으로 AGV에게 STOP 또는 PAUSE 명령 전송
    }

    @Transactional
    public void resumeAgv(Integer agvId) {
        Agv agv = findAgv(agvId);

        agv.setStatus(AgvStatus.IDLE);
        agv.setLastSeenAt(LocalDateTime.now());

        // TODO: WebSocket으로 AGV에게 RESUME 명령 전송
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