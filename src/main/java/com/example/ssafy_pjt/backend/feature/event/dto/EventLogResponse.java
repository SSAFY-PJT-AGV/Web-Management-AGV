package com.example.ssafy_pjt.backend.feature.event.dto;

import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventLogResponse {
    private final Long eventId;
    private final String eventType;
    private final String level;
    private final String message;
    private final String targetType;
    private final String targetId;
    private final LocalDateTime createdAt;

    public EventLogResponse(EventLog eventLog) {
        this.eventId = eventLog.getEventId();
        this.eventType = eventLog.getEventType().name();
        this.level = eventLog.getLevel().name();
        this.message = eventLog.getMessage();
        this.targetType = eventLog.getTargetType();
        this.targetId = eventLog.getTargetId();
        this.createdAt = eventLog.getCreatedAt();
    }
}
