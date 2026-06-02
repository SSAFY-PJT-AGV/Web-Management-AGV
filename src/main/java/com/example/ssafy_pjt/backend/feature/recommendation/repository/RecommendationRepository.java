package com.example.ssafy_pjt.backend.feature.recommendation.repository;

import com.example.ssafy_pjt.backend.feature.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findAllByOrderByPriorityRankAsc();
}