package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.reservation.service.ReservationService;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.enums.EventLevel;
import com.example.ssafy_pjt.backend.feature.event.enums.EventType;
import com.example.ssafy_pjt.backend.feature.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionResultService {

    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;
    private final ReservationService reservationService;
    private final DashboardBroadcastService dashboardBroadcastService;
    private final InventoryRepository inventoryRepository;
    private final ArucoMarkerRepository arucoMarkerRepository;
    private final EventLogService eventLogService;

    @Transactional
    public void handleAgvDone(AgvStatusMessage message) {
        System.out.println(
                "[MISSION DONE REQUEST]"
                        + " agvId=" + message.getAgvId()
                        + ", commandId=" + message.getCommandId()
                        + ", status=" + message.getStatus()
                        + ", event=" + message.getEvent()
        );

        Mission mission = missionRepository.findById(message.getCommandId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mission 없음 commandId=" + message.getCommandId()
                ));

        Agv agv = agvRepository.findById(message.getAgvId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "AGV 없음 agvId=" + message.getAgvId()
                ));

        if (mission.getStatus() == MissionStatus.COMPLETED) {
            System.out.println(
                    "[MISSION DONE DUPLICATE SKIP]"
                            + " missionId=" + mission.getMissionId()
                            + ", agvId=" + agv.getAgvId()
            );
            return;
        }

        handleSuccess(mission, agv);
    }

    private void handleSuccess(Mission mission, Agv agv) {
        mission.setStatus(MissionStatus.COMPLETED);
        mission.setCompletedAt(LocalDateTime.now());

        updateReservationByCompletedMission(mission, agv);
        updateInventoryByCompletedMission(mission);
        updateBoxStateByCompletedMission(mission);

        agv.setStatus(AgvStatus.IDLE);
        agv.setCurrentMission(null);
        agv.setLastSeenAt(LocalDateTime.now());

        System.out.println(
                "[MISSION COMPLETE] mission="
                        + mission.getMissionId()
                        + ", agv="
                        + agv.getAgvId()
                        + ", type="
                        + mission.getMissionType()
        );

        eventLogService.create(
                EventLog.create(
                        EventType.MISSION_COMPLETED,
                        EventLevel.INFO,
                        "Mission 완료: AGV"
                                + agv.getAgvId()
                                + ", mission="
                                + mission.getMissionId()
                                + ", type="
                                + mission.getMissionType(),
                        "MISSION",
                        mission.getMissionId()
                )
        );

        dashboardBroadcastService.missionRefresh();
        dashboardBroadcastService.inventoryRefresh();
        dashboardBroadcastService.mapRefresh();
        dashboardBroadcastService.agvRefresh();
    }

    private void updateReservationByCompletedMission(Mission mission, Agv agv) {
        MissionType type = mission.getMissionType();

        if (
                type == MissionType.DROP_TO_CROSS ||
                        type == MissionType.PICK_FROM_CROSS
        ) {
            reservationService.releaseCrossZone(agv, mission);
        }
    }

    private void updateInventoryByCompletedMission(Mission mission) {
        System.out.println(
                "[INVENTORY CHECK]"
                        + " missionId=" + mission.getMissionId()
                        + ", type=" + mission.getMissionType()
                        + ", material="
                        + (mission.getMaterial() == null
                        ? null
                        : mission.getMaterial().getMaterialCode())
                        + ", quantity=" + mission.getQuantity()
        );

        if (mission.getMaterial() == null || mission.getQuantity() == null) {
            return;
        }

        if (mission.getMissionType() == MissionType.DROP_TO_CONVEYOR) {

            Inventory inventory = inventoryRepository
                    .findByMaterialAndZone(
                            mission.getMaterial(),
                            mission.getSourceZone()
                    )
                    .orElseThrow(() ->
                            new IllegalStateException("재고 없음")
                    );

            int beforeQuantity = inventory.getCurrentQuantity();

            int nextQuantity = Math.max(
                    0,
                    beforeQuantity - mission.getQuantity()
            );

            inventory.setCurrentQuantity(nextQuantity);
            inventory.setStatus(resolveInventoryStatus(inventory));

            if (nextQuantity == 0) {
                arucoMarkerRepository
                        .findByMarkerTypeAndMaterial_MaterialIdAndEmptyStatus(
                                MarkerType.MATERIAL_BOX,
                                mission.getMaterial().getMaterialId(),
                                false
                        )
                        .ifPresent(marker -> {
                            marker.setEmptyStatus(true);

                            System.out.println(
                                    "[BOX EMPTY]"
                                            + " material="
                                            + mission.getMaterial().getMaterialCode()
                                            + ", marker="
                                            + marker.getMarkerId()
                            );
                        });
            }

            System.out.println(
                    "[INVENTORY UPDATED]"
                            + " material="
                            + mission.getMaterial().getMaterialCode()
                            + ", before="
                            + beforeQuantity
                            + ", after="
                            + nextQuantity
            );

            eventLogService.create(
                    EventLog.create(
                            EventType.INVENTORY_UPDATED,
                            nextQuantity <= 0 ? EventLevel.WARNING : EventLevel.INFO,
                            "재고 변경: material="
                                    + mission.getMaterial().getMaterialCode()
                                    + ", before="
                                    + beforeQuantity
                                    + ", after="
                                    + nextQuantity,
                            "INVENTORY",
                            inventory.getInventoryId()
                    )
            );

            return;
        }

        if (mission.getMissionType() == MissionType.DROP_TO_STORAGE) {
            Inventory inventory = inventoryRepository
                    .findByMaterialAndZone(mission.getMaterial(), mission.getTargetZone())
                    .orElseThrow(() -> new IllegalStateException(
                            "보급할 재고를 찾을 수 없습니다. material="
                                    + mission.getMaterial().getMaterialCode()
                    ));

            int beforeQuantity = inventory.getCurrentQuantity();
            int nextQuantity = beforeQuantity + mission.getQuantity();

            inventory.setCurrentQuantity(nextQuantity);
            inventory.setStatus(resolveInventoryStatus(inventory));

            System.out.println(
                    "[INVENTORY UPDATED] type=DROP_TO_STORAGE"
                            + ", material=" + mission.getMaterial().getMaterialCode()
                            + ", before=" + beforeQuantity
                            + ", after=" + nextQuantity
            );
        }
    }

    private void updateBoxStateByCompletedMission(Mission mission) {
        MissionType type = mission.getMissionType();

        if (type == MissionType.PICK_EMPTY_BOX) {
            reserveFirstEmptyProductBox(mission);
            return;
        }

        if (type == MissionType.DROP_TO_CONVEYOR) {
            markMaterialBoxEmpty(mission);
            return;
        }

        if (type == MissionType.DROP_TO_STORAGE) {
            markMaterialBoxFilled(mission);
            return;
        }

        if (type == MissionType.DROP_TO_OUTBOUND) {
            markProductBoxEmptyAfterOutbound(mission);
        }
    }

    private void reserveFirstEmptyProductBox(Mission mission) {
        if (mission.getProduct() == null) {
            return;
        }

        arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductIdAndEmptyStatus(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId(),
                        true
                )
                .stream()
                .findFirst()
                .ifPresent(marker -> {
                    marker.setEmptyStatus(false);

                    System.out.println(
                            "[PRODUCT BOX RESERVED]"
                                    + " productId="
                                    + mission.getProduct().getProductId()
                                    + ", marker="
                                    + marker.getMarkerId()
                    );
                });
    }

    private void markProductBoxEmptyAfterOutbound(Mission mission) {
        if (mission.getProduct() == null) {
            return;
        }

        arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductIdAndEmptyStatus(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId(),
                        false
                )
                .stream()
                .findFirst()
                .ifPresent(marker -> {
                    marker.setEmptyStatus(true);

                    System.out.println(
                            "[PRODUCT BOX EMPTY AFTER OUTBOUND]"
                                    + " productId="
                                    + mission.getProduct().getProductId()
                                    + ", marker="
                                    + marker.getMarkerId()
                    );
                });
    }

    private void markMaterialBoxEmpty(Mission mission) {
        if (mission.getMaterial() == null) {
            return;
        }

        arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialIdAndEmptyStatus(
                        MarkerType.MATERIAL_BOX,
                        mission.getMaterial().getMaterialId(),
                        false
                )
                .ifPresent(marker -> {
                    marker.setEmptyStatus(true);

                    System.out.println(
                            "[MATERIAL BOX EMPTY]"
                                    + " material="
                                    + mission.getMaterial().getMaterialCode()
                                    + ", marker="
                                    + marker.getMarkerId()
                    );
                });
    }

    private void markMaterialBoxFilled(Mission mission) {
        if (mission.getMaterial() == null) {
            return;
        }

        Inventory inventory = inventoryRepository
                .findByMaterialAndZone(
                        mission.getMaterial(),
                        mission.getTargetZone()
                )
                .orElse(null);

        if (inventory == null) {
            return;
        }

        int currentQuantity =
                inventory.getCurrentQuantity() == null
                        ? 0
                        : inventory.getCurrentQuantity();

        if (currentQuantity <= 0) {
            return;
        }

        arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialIdAndEmptyStatus(
                        MarkerType.MATERIAL_BOX,
                        mission.getMaterial().getMaterialId(),
                        true
                )
                .ifPresent(marker -> {
                    marker.setEmptyStatus(false);

                    System.out.println(
                            "[MATERIAL BOX FILLED]"
                                    + " material="
                                    + mission.getMaterial().getMaterialCode()
                                    + ", marker="
                                    + marker.getMarkerId()
                    );
                });
    }

    private InventoryStatus resolveInventoryStatus(Inventory inventory) {
        int current = inventory.getCurrentQuantity() == null ? 0 : inventory.getCurrentQuantity();
        int reserved = inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
        int min = inventory.getMinThreshold() == null ? 0 : inventory.getMinThreshold();

        int available = current - reserved;

        if (available <= 0) {
            return InventoryStatus.SHORTAGE;
        }

        if (available <= min) {
            return InventoryStatus.LOW;
        }

        return InventoryStatus.NORMAL;
    }
}