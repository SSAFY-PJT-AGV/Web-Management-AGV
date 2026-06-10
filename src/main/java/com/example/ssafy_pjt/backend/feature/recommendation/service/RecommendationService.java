package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.recommendation.dto.RecommendationResponse;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final DashboardSender dashboardSender;


    public List<RecommendationResponse> getRecommendations() {

        // TODO:
        // 추후 inventory/event/mission 데이터 기반 AI 추천으로 교체
        return List.of(
                new RecommendationResponse(
                        "WARN",
                        "CHIP 재고가 빠르게 감소하고 있습니다."
                ),
                new RecommendationResponse(
                        "INFO",
                        "AGV01이 현재 자재 공급 작업을 수행 중입니다."
                )
        );
    }


    public void updateRecommendation() {

        List<RecommendationResponse> result =
                getRecommendations();


        Map<String, Object> payload =
                new HashMap<>();

        payload.put(
                "type",
                "AI_RECOMMENDATION_UPDATED"
        );

        payload.put(
                "data",
                result
        );


        dashboardSender.broadcast(payload);
    }
}