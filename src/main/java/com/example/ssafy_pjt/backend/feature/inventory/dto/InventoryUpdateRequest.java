package com.example.ssafy_pjt.backend.feature.inventory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryUpdateRequest {
    private Integer currentQuantity;
    private Integer reservedQuantity;
}
