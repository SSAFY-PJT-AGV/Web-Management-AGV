package com.example.ssafy_pjt.backend.feature.event.service;

import com.example.ssafy_pjt.backend.feature.event.dto.EventLogResponse;
import com.example.ssafy_pjt.backend.feature.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventLogService {
    private final EventLogRepository eventLogRepository;

    @Transactional(readOnly = true)
    public List<EventLogResponse> getEvents() {
        return eventLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(EventLogResponse::new)
                .toList();
    }
}
