package com.example.ssafy_pjt.backend.feature.inventory.entity;

import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @Column(name = "min_threshold", nullable = false)
    private Integer minThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryStatus status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public int calculateAvailableQuantity() {
        return currentQuantity - reservedQuantity;
    }

    public boolean isShortage() {
        return calculateAvailableQuantity() < minThreshold;
    }

    public void decreaseStock(Integer quantity) {
        this.currentQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }
}
