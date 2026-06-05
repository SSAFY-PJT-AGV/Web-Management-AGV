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
}
