package com.example.ssafy_pjt.backend.feature.recommendation.scheduler;

import com.example.ssafy_pjt.backend.feature.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final RecommendationService recommendationService;

    @Scheduled(fixedDelay = 5000)
    public void ruleCheck() {
        recommendationService.analyzeRule();
    }


    @Scheduled(fixedDelay = 20000)
    public void aiCheck() {
        recommendationService.analyzeAi();
    }
}