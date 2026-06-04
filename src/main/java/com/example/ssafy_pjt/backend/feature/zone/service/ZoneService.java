package com.example.ssafy_pjt.backend.feature.zone.service;

import com.example.ssafy_pjt.backend.feature.zone.dto.ZoneResponse;
import com.example.ssafy_pjt.backend.feature.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public List<ZoneResponse> getZones() {
        return zoneRepository.findAll()
                .stream()
                .map(ZoneResponse::new)
                .toList();
    }
}
