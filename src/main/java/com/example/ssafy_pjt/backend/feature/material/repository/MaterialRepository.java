package com.example.ssafy_pjt.backend.feature.material.repository;

import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByMaterialCode(String materialCode);
}