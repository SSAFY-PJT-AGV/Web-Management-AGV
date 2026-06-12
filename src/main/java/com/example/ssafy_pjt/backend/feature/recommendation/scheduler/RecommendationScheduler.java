package com.example.ssafy_pjt.backend.feature.recommendation.scheduler;

import com.example.ssafy_pjt.backend.feature.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final RecommendationService recommendationService;

    @Scheduled(fixedDelay = 10000)
    public void run() {
        recommendationService.analyze();
    }
}