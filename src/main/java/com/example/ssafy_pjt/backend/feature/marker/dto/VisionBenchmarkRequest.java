package com.example.ssafy_pjt.backend.feature.marker.dto;

public record VisionBenchmarkRequest(
        Integer agvId,
        String imageBase64,
        Integer repeat
) {
}