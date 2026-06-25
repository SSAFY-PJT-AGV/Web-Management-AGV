package com.example.ssafy_pjt.backend.feature.recommendation.scheduler;

import com.example.ssafy_pjt.backend.feature.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final RecommendationService recommendationService;


    /*
     * Rule 기반 분석
     *
     * 내부 로직이므로 실패하면 확인 필요
     */
    @Scheduled(fixedDelay = 5000)
    public void ruleCheck() {

        try {
            recommendationService.analyzeRule();

        } catch (Exception e) {

            log.warn(
                    "[RULE AI SKIP] reason={}",
                    e.getMessage()
            );
        }
    }


    /*
     * LLM 기반 운영 분석
     *
     * 외부 AI API 장애가 발생해도
     * AGV 관제 시스템은 계속 동작해야 한다.
     */
    @Scheduled(fixedDelay = 20000)
    public void aiCheck() {

        try {

            recommendationService.analyzeAi();

        } catch (Exception e) {

            log.warn(
                    "[LLM AI UNAVAILABLE] AGV control continues. reason={}",
                    e.getMessage()
            );
        }
    }
}