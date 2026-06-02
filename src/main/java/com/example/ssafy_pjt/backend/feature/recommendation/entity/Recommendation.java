package com.example.ssafy_pjt.backend.feature.recommendation.entity;

import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Getter
@Setter
@NoArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "priority_rank", nullable = false)
    private Integer priorityRank;

    @Column(name = "priority_score", nullable = false)
    private Double priorityScore;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
