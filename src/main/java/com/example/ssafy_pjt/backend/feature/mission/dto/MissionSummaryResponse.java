package com.example.ssafy_pjt.backend.feature.mission.dto;

public record MissionSummaryResponse(
        Integer order,
        String jobName,
        Integer agvId,
        String agv,
        String status
) {}