package com.example.ssafy_pjt.backend.feature.event.entity;

import com.example.ssafy_pjt.backend.feature.event.enums.EventLevel;
import com.example.ssafy_pjt.backend.feature.event.enums.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_log")
@Getter
@Setter
@NoArgsConstructor
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;


    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventLevel level;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;


    @Column(name = "target_type", length = 50)
    private String targetType;


    @Column(name = "target_id", length = 50)
    private String targetId;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public static EventLog create(
            EventType eventType,
            EventLevel level,
            String message,
            String targetType,
            Object targetId
    ) {
        EventLog log = new EventLog();

        log.eventType = eventType;
        log.level = level;
        log.message = message;
        log.targetType = targetType;

        if (targetId != null) {
            log.targetId = String.valueOf(targetId);
        }

        return log;
    }


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}