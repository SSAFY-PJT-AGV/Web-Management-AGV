package com.example.ssafy_pjt.backend.feature.inventory.dto;

import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InventoryResponse {

    private final Long inventoryId;
    private final String materialCode;
    private final String materialName;
    private final String zoneName;
    private final Integer currentQuantity;
    private final Integer reservedQuantity;
    private final Integer minThreshold;
    private final String status;
    private final LocalDateTime updatedAt;

    public InventoryResponse(Inventory inventory) {
        this.inventoryId = inventory.getInventoryId();
        this.materialCode = inventory.getMaterial().getMaterialCode();
        this.materialName = inventory.getMaterial().getMaterialName();
        this.zoneName = inventory.getZone().getZoneName();
        this.currentQuantity = inventory.getCurrentQuantity();
        this.reservedQuantity = inventory.getReservedQuantity();
        this.minThreshold = inventory.getMinThreshold();
        this.status = inventory.getStatus().name();
        this.updatedAt = inventory.getUpdatedAt();
    }
}