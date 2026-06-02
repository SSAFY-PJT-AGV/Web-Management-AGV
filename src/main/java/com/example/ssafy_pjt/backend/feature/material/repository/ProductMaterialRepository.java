package com.example.ssafy_pjt.backend.feature.material.repository;

import com.example.ssafy_pjt.backend.feature.material.entity.ProductMaterial;
import com.example.ssafy_pjt.backend.feature.task.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {
    List<ProductMaterial> findByProductType(ProductType productType);
}