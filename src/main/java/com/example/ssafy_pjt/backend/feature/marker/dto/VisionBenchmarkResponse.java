package com.example.ssafy_pjt.backend.feature.marker.dto;

public record VisionBenchmarkResponse(
        int repeat,

        long legacyAvgMs,
        long legacyMaxMs,
        long legacyMinMs,
        int legacyDetectedCount,

        long currentAvgMs,
        long currentMaxMs,
        long currentMinMs,
        int currentDetectedCount,

        double improvementRate
) {
}