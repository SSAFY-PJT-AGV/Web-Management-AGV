package com.example.ssafy_pjt.backend.feature.event.controller;

import com.example.ssafy_pjt.backend.feature.event.dto.EventLogResponse;
import com.example.ssafy_pjt.backend.feature.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventLogController {
    private final EventLogService eventLogService;

    @GetMapping
    public List<EventLogResponse> getEvents() {
        return eventLogService.getEvents();
    }
}
