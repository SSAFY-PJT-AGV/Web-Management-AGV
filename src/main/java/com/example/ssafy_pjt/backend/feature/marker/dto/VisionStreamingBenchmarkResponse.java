package com.example.ssafy_pjt.backend.feature.marker.dto;

public record VisionStreamingBenchmarkResponse(
        int fps,
        int durationSec,
        int sentFrameCount,

        int legacyProcessedCount,
        long legacyTotalElapsedMs,
        long legacyAvgProcessMs,
        long legacyMaxProcessMs,

        int currentProcessedCount,
        int currentDroppedEstimate,
        long currentTotalElapsedMs,
        long currentAvgCallbackDelayMs,
        long currentMaxCallbackDelayMs
) {
}