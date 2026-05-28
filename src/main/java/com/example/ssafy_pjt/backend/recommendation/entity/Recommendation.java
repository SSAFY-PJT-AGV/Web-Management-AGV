package com.example.ssafy_pjt.backend.recommendation.entity;

import com.example.ssafy_pjt.backend.material.entity.Material;
import com.example.ssafy_pjt.backend.task.entity.ProductionTask;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ProductionTask productionTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Material material;

    @Column(name = "priority_score", nullable = false)
    private Double priorityScore;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
