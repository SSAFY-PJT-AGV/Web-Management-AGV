package com.example.ssafy_pjt.backend.feature.inventory.service;

import com.example.ssafy_pjt.backend.feature.inventory.dto.InventoryResponse;
import com.example.ssafy_pjt.backend.feature.inventory.dto.InventoryUpdateRequest;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.mission.service.ReplenishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReplenishmentService replenishmentService;

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventories() {
        return inventoryRepository.findAll()
                .stream()
                .map(InventoryResponse::new)
                .toList();
    }

    @Transactional
    public InventoryResponse updateQuantity(
            Long inventoryId,
            InventoryUpdateRequest request
    ) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다."));

        InventoryStatus previousStatus = inventory.getStatus();

        if (request.getCurrentQuantity() != null) {
            inventory.setCurrentQuantity(request.getCurrentQuantity());
        }

        if (request.getReservedQuantity() != null) {
            inventory.setReservedQuantity(request.getReservedQuantity());
        }

        InventoryStatus newStatus = calculateStatus(inventory);

        inventory.setStatus(newStatus);
        inventory.setUpdatedAt(LocalDateTime.now());

        if (shouldCreateReplenishmentMission(previousStatus, newStatus)) {
            replenishmentService.createReplenishmentMissions(
                    inventory.getMaterial().getMaterialCode(),
                    inventory.getMinThreshold()
            );
        }

        return new InventoryResponse(inventory);
    }

    private boolean shouldCreateReplenishmentMission(
            InventoryStatus previousStatus,
            InventoryStatus newStatus
    ) {
        return previousStatus != InventoryStatus.SHORTAGE
                && newStatus == InventoryStatus.SHORTAGE;
    }

    private InventoryStatus calculateStatus(Inventory inventory) {
        if (inventory.getCurrentQuantity() <= 0) {
            return InventoryStatus.SHORTAGE;
        }

        if (inventory.getCurrentQuantity() <= inventory.getMinThreshold()) {
            return InventoryStatus.LOW;
        }

        return InventoryStatus.NORMAL;
    }
}