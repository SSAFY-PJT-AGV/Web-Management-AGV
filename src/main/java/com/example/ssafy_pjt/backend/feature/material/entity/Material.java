package com.example.ssafy_pjt.backend.feature.material.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "material")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private int materialId;

    @Column(name = "material_code", nullable = false, unique = true, length = 50)
    private String pmaterialCode;

    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName;

    public void changeName(String materialName) {
        this.materialName = materialName;
    }
}
