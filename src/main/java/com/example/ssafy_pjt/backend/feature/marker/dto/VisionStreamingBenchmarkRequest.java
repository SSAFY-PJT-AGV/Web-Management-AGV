package com.example.ssafy_pjt.backend.feature.marker.dto;

public record VisionStreamingBenchmarkRequest(
        Integer agvId,
        String imageBase64,
        Integer fps,
        Integer durationSec
) {
}