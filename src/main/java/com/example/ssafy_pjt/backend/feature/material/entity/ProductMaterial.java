package com.example.ssafy_pjt.backend.feature.material.entity;

import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_material")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ProductionTask productionTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;
}
