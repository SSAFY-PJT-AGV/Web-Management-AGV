package com.example.ssafy_pjt.backend.feature.agv.service;

import com.example.ssafy_pjt.backend.feature.agv.dto.AgvResponse;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgvService {
    private final AgvRepository agvRepository;

    @Transactional(readOnly = true)
    public List<AgvResponse> getAgvs() {
        return agvRepository.findAll()
                .stream()
                .map(AgvResponse::new)
                .toList();
    }

    public void pauseAgv(Integer agvId) {
        // TODO: AGV 상태 WAITING/PAUSED 처리
        // TODO: WebSocket으로 AGV에게 STOP 또는 PAUSE 명령 전송
    }

    public void resumeAgv(Integer agvId) {
        // TODO: AGV 상태 재개 처리
        // TODO: WebSocket으로 AGV에게 RESUME 명령 전송
    }

    public void cancelCurrentTask(Integer agvId) {
        // TODO: 현재 Mission 취소 처리
        // TODO: AGV에게 STOP/CANCEL 명령 전송
    }
}
