package com.example.ssafy_pjt.backend.feature.event.repository;

import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findTop50ByOrderByCreatedAtDesc();
}