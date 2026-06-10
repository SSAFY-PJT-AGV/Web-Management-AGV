package com.example.ssafy_pjt.backend.feature.marker.dto;

public record AgvMapItem(
        String agvId,
        Integer currentMarker,
        String status
) {}