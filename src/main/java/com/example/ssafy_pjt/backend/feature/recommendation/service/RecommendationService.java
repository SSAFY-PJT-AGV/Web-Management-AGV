package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.recommendation.dto.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    public List<RecommendationResponse> getRecommendations() {
        // TODO: 임시 Mock 응답
        // 추후 inventory/event/mission 데이터를 기반으로 추천 로직으로 교체 예정
        return List.of(
                new RecommendationResponse("WARN", "CHIP 재고가 빠르게 감소하고 있습니다."),
                new RecommendationResponse("INFO", "AGV01이 현재 자재 공급 작업을 수행 중입니다.")
        );
    }
}