package com.example.ssafy_pjt.backend.feature.system.service;

import com.example.ssafy_pjt.backend.feature.system.entity.SystemState;
import com.example.ssafy_pjt.backend.feature.system.enums.OperationMode;
import com.example.ssafy_pjt.backend.feature.system.enums.ScenarioStatus;
import com.example.ssafy_pjt.backend.feature.system.repository.SystemStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemStateService {

    private static final Long SYSTEM_STATE_ID = 1L;

    private final SystemStateRepository systemStateRepository;


    public SystemState getCurrentState() {
        return systemStateRepository.findById(SYSTEM_STATE_ID)
                .orElseThrow(() ->
                        new IllegalStateException("SystemState가 초기화되지 않았습니다.")
                );
    }


    @Transactional
    public void changeMode(OperationMode mode) {
        SystemState state = getCurrentState();

        state.setMode(mode);
        state.setUpdatedAt(LocalDateTime.now());
    }


    @Transactional
    public void changeScenarioStatus(ScenarioStatus scenarioStatus) {
        SystemState state = getCurrentState();

        state.setScenarioStatus(scenarioStatus);
        state.setUpdatedAt(LocalDateTime.now());
    }


    @Transactional
    public void markRunning() {
        changeScenarioStatus(ScenarioStatus.RUNNING);
    }


    @Transactional
    public void markReady() {
        SystemState state = getCurrentState();

        state.setScenarioStatus(ScenarioStatus.READY);
        state.setLastResetAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
    }


    @Transactional
    public void markError() {
        changeScenarioStatus(ScenarioStatus.ERROR);
    }
}