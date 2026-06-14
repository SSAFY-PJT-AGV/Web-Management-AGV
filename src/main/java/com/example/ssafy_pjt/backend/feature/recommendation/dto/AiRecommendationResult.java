package com.example.ssafy_pjt.backend.feature.recommendation.dto;

public record AiRecommendationResult(
        String title,
        String message,
        double priorityScore,
        String target,
        String targetKey
) {
}