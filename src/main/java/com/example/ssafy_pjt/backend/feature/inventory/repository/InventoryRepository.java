package com.example.ssafy_pjt.backend.feature.inventory.repository;

import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMaterialAndZone(Material material, Zone zone);

    Optional<Inventory> findByMaterial_MaterialCode(String materialCode);

    Optional<Inventory> findByMaterial_MaterialId(Long materialId);
}