package com.example.ssafy_pjt.backend.feature.event.service;

import com.example.ssafy_pjt.backend.feature.event.dto.EventLogResponse;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.repository.EventLogRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final DashboardSender dashboardSender;


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

        EventLog saved =
                eventLogRepository.save(eventLog);

        broadcast(saved);
    }


    private void broadcast(EventLog eventLog) {

        Map<String, Object> payload =
                new HashMap<>();

        payload.put(
                "type",
                "EVENT_LOG_CREATED"
        );

        payload.put(
                "data",
                new EventLogResponse(eventLog)
        );


        dashboardSender.broadcast(payload);
    }
}