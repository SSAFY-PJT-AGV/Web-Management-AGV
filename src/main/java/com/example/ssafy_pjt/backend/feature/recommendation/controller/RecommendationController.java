package com.example.ssafy_pjt.backend.feature.recommendation.controller;

import com.example.ssafy_pjt.backend.feature.recommendation.dto.RecommendationResponse;
import com.example.ssafy_pjt.backend.feature.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<RecommendationResponse> getRecommendations() {
        return recommendationService.getRecommendations();
    }
}