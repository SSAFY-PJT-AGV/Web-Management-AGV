package com.example.ssafy_pjt.backend.feature.mission.dto;

public record MissionSummaryResponse(
        Integer order,
        String jobName,
        String agv,
        String status
) {}