package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.reservation.service.ReservationService;
import com.example.ssafy_pjt.backend.websocket.dto.AgvStatusMessage;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
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

    @Transactional
    public void handleAgvDone(AgvStatusMessage message) {
        Mission mission = missionRepository.findById(message.getCommandId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mission 없음 commandId=" + message.getCommandId()
                ));

        Agv agv = agvRepository.findById(message.getAgvId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "AGV 없음 agvId=" + message.getAgvId()
                ));

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

        System.out.println(
                "[MISSION COMPLETE] mission="
                        + mission.getMissionId()
                        + ", agv="
                        + agv.getAgvId()
        );

        dashboardBroadcastService.missionRefresh();
        dashboardBroadcastService.mapRefresh();
        dashboardBroadcastService.agvRefresh();

    }

    private void updateBoxStateByCompletedMission(Mission mission) {

        MissionType type = mission.getMissionType();


        // 자재를 컨베이어에 투입 완료
        // 자재 박스가 비게 됨
        if (type == MissionType.DROP_TO_CONVEYOR) {

            if (mission.getMaterial() == null) {
                return;
            }

            ArucoMarker marker =
                    arucoMarkerRepository
                            .findByMarkerTypeAndMaterial_MaterialId(
                                    MarkerType.MATERIAL_BOX,
                                    mission.getMaterial().getMaterialId()
                            )
                            .orElseThrow();

            marker.setEmpty(true);
            return;
        }


        // 보급 완료
        // 꽉 찬 자재 박스가 들어옴
        if (type == MissionType.DROP_TO_STORAGE) {

            if (mission.getMaterial() == null) {
                return;
            }

            ArucoMarker marker =
                    arucoMarkerRepository
                            .findByMarkerTypeAndMaterial_MaterialId(
                                    MarkerType.MATERIAL_BOX,
                                    mission.getMaterial().getMaterialId()
                            )
                            .orElseThrow();

            marker.setEmpty(false);
            return;
        }



        // 완제품 보관소에 하역 완료
        // 빈 제품 박스 → 꽉 찬 제품 박스
        if (type == MissionType.DROP_TO_FINISHED_BOX_STORAGE) {

            if (mission.getProduct() == null) {
                return;
            }

            ArucoMarker marker =
                    arucoMarkerRepository
                            .findByMarkerTypeAndProduct_ProductId(
                                    MarkerType.PRODUCT_TYPE,
                                    mission.getProduct().getProductId()
                            )
                            .orElseThrow();

            marker.setEmpty(false);
            return;
        }


        // 출고 완료
        // 완제품 박스가 다시 빈 박스가 됨
        if (type == MissionType.DROP_TO_OUTBOUND) {

            if (mission.getProduct() == null) {
                return;
            }

            ArucoMarker marker =
                    arucoMarkerRepository
                            .findByMarkerTypeAndProduct_ProductId(
                                    MarkerType.PRODUCT_TYPE,
                                    mission.getProduct().getProductId()
                            )
                            .orElseThrow();

            marker.setEmpty(true);
        }
    }

    private void updateInventoryByCompletedMission(Mission mission) {
        if (mission.getMaterial() == null || mission.getQuantity() == null) {
            return;
        }

        if (mission.getMissionType() == MissionType.DROP_TO_CONVEYOR) {
            Inventory inventory = inventoryRepository
                    .findByMaterialAndZone(mission.getMaterial(), mission.getSourceZone())
                    .orElseThrow(() -> new IllegalStateException(
                            "차감할 재고를 찾을 수 없습니다. material="
                                    + mission.getMaterial().getMaterialCode()
                    ));

            int nextQuantity = Math.max(
                    0,
                    inventory.getCurrentQuantity() - mission.getQuantity()
            );

            inventory.setCurrentQuantity(nextQuantity);
            inventory.setStatus(resolveInventoryStatus(inventory));
            return;
        }

        if (mission.getMissionType() == MissionType.DROP_TO_STORAGE) {
            Inventory inventory = inventoryRepository
                    .findByMaterialAndZone(mission.getMaterial(), mission.getTargetZone())
                    .orElseThrow(() -> new IllegalStateException(
                            "보급할 재고를 찾을 수 없습니다. material="
                                    + mission.getMaterial().getMaterialCode()
                    ));

            inventory.setCurrentQuantity(
                    inventory.getCurrentQuantity() + mission.getQuantity()
            );
            inventory.setStatus(resolveInventoryStatus(inventory));
        }
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

    private void updateReservationByCompletedMission(Mission mission, Agv agv) {
        MissionType type = mission.getMissionType();

        if (type == MissionType.DROP_TO_CROSS) {
            reservationService.occupyCrossZone(agv, mission);
            return;
        }

        if (type == MissionType.PICK_FROM_CROSS
                || type == MissionType.DROP_TO_STORAGE) {
            reservationService.releaseCrossZone(agv, mission);
        }
    }
}