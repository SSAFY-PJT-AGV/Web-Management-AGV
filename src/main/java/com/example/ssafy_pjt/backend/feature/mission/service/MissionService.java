package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.mission.dto.MissionResponse;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public List<MissionResponse> getMissions() {
        return missionRepository.findAll()
                .stream()
                .map(MissionResponse::new)
                .toList();
    }
}
