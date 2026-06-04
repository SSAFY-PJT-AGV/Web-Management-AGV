package com.example.ssafy_pjt.backend.feature.inventory.controller;

import com.example.ssafy_pjt.backend.feature.inventory.dto.InventoryUpdateRequest;
import com.example.ssafy_pjt.backend.feature.inventory.dto.InventoryResponse;
import com.example.ssafy_pjt.backend.feature.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryResponse> getInventories() {
        return inventoryService.getInventories();
    }

    @PatchMapping("/{inventoryId}")
    public InventoryResponse updateQuantity(
            @PathVariable Long inventoryId,
            @RequestBody InventoryUpdateRequest request
    ) {
        return inventoryService.updateQuantity(inventoryId, request);
    }
}