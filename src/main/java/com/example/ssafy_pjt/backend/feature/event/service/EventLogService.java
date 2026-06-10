package com.example.ssafy_pjt.backend.feature.event.service;

import com.example.ssafy_pjt.backend.feature.event.dto.EventLogResponse;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.repository.EventLogRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final DashboardBroadcastService dashboardBroadcastService;

    @Transactional(readOnly = true)
    public List<EventLogResponse> getEvents() {
        return eventLogRepository
                .findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(EventLogResponse::new)
                .toList();
    }

    @Transactional
    public void create(EventLog eventLog) {
        eventLogRepository.save(eventLog);

        dashboardBroadcastService.eventRefresh();
    }
}