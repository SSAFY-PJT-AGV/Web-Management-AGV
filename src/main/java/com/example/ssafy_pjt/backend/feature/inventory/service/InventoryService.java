package com.example.ssafy_pjt.backend.feature.inventory.service;

import com.example.ssafy_pjt.backend.feature.inventory.dto.InventoryResponse;
import com.example.ssafy_pjt.backend.feature.inventory.dto.InventoryUpdateRequest;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.mission.service.ReplenishmentService;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardSender;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.enums.EventLevel;
import com.example.ssafy_pjt.backend.feature.event.enums.EventType;
import com.example.ssafy_pjt.backend.feature.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReplenishmentService replenishmentService;
    private final DashboardSender dashboardSender;
    private final DashboardBroadcastService dashboardBroadcastService;
    private final EventLogService eventLogService;

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
            dashboardBroadcastService.inventoryRefresh();
        }

        if (request.getReservedQuantity() != null) {
            inventory.setReservedQuantity(request.getReservedQuantity());
        }

        InventoryStatus newStatus = calculateStatus(inventory);

        eventLogService.create(
                EventLog.create(
                        EventType.INVENTORY_UPDATED,
                        newStatus == InventoryStatus.SHORTAGE
                                ? EventLevel.WARNING
                                : EventLevel.INFO,
                        "재고 수량 변경: material="
                                + inventory.getMaterial().getMaterialCode()
                                + ", current="
                                + inventory.getCurrentQuantity()
                                + ", reserved="
                                + inventory.getReservedQuantity()
                                + ", status="
                                + newStatus,
                        "INVENTORY",
                        inventory.getInventoryId()
                )
        );

        inventory.setStatus(newStatus);
        inventory.setUpdatedAt(LocalDateTime.now());

        InventoryResponse response = new InventoryResponse(inventory);

        broadcastInventoryUpdated();

        if (shouldCreateReplenishmentMission(previousStatus, newStatus)) {
            replenishmentService.createReplenishmentMissions(
                    inventory.getMaterial().getMaterialCode(),
                    inventory.getMinThreshold()
            );
        }

        return response;
    }

    private void broadcastInventoryUpdated() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "INVENTORY_UPDATED");
        payload.put("data", getInventories());

        dashboardSender.broadcast(payload);
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