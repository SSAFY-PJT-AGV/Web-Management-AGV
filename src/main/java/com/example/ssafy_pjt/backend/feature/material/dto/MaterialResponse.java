package com.example.ssafy_pjt.backend.feature.material.dto;

import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import lombok.Getter;

@Getter
public class MaterialResponse {

    private final Long materialId;
    private final String materialCode;
    private final String materialName;
    private final String description;

    public MaterialResponse(Material material) {
        this.materialId = material.getMaterialId();
        this.materialCode = material.getMaterialCode();
        this.materialName = material.getMaterialName();
        this.description = material.getDescription();
    }
}