package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkerResolveService {

    private final ArucoMarkerRepository arucoMarkerRepository;

    public Integer resolveDestinationMarkerId(Mission mission) {
        MissionType type = mission.getMissionType();

        return switch (type) {
            case PICK_FROM_STORAGE ->
                    resolveMaterialBoxMarker(mission);

            case DROP_TO_CONVEYOR ->
                    resolveZoneMarker("CONVEYOR_START");

            case PICK_FROM_CONVEYOR ->
                    resolveZoneMarker("CONVEYOR_END");

            case DROP_TO_FINISHED_BOX_STORAGE ->
                    resolveProductBoxMarker(mission);

            case PICK_FROM_INBOUND ->
                    resolveZoneMarker("INBOUND");

            case DROP_TO_OUTBOUND ->
                    resolveZoneMarker("OUTBOUND");

            case PICK_FROM_CROSS, DROP_TO_CROSS ->
                    resolveZoneMarker("CROSS_ZONE");

            case RETURN_TO_BASE ->
                    resolveReturnBaseMarker(mission);

            default ->
                    throw new IllegalArgumentException("지원하지 않는 MissionType입니다: " + type);
        };
    }

    private Integer resolveMaterialBoxMarker(Mission mission) {
        if (mission.getMaterial() == null) {
            throw new IllegalStateException("자재 박스 미션인데 material이 없습니다. missionId=" + mission.getMissionId());
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialId(
                        MarkerType.MATERIAL_BOX,
                        mission.getMaterial().getMaterialId()
                )
                .orElseThrow(() -> new IllegalStateException("자재 박스 마커를 찾을 수 없습니다. materialId="
                        + mission.getMaterial().getMaterialId()))
                .getMarkerId();
    }

    private Integer resolveProductBoxMarker(Mission mission) {
        if (mission.getProduct() == null) {
            throw new IllegalStateException("완제품 박스 미션인데 product가 없습니다. missionId=" + mission.getMissionId());
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductId(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId()
                )
                .orElseThrow(() -> new IllegalStateException("완제품 박스 마커를 찾을 수 없습니다. productId="
                        + mission.getProduct().getProductId()))
                .getMarkerId();
    }

    private Integer resolveZoneMarker(String zoneName) {
        return arucoMarkerRepository
                .findByMarkerTypeAndZone_ZoneName(MarkerType.ZONE, zoneName)
                .orElseThrow(() -> new IllegalStateException("Zone 마커를 찾을 수 없습니다. zoneName=" + zoneName))
                .getMarkerId();
    }

    private Integer resolveReturnBaseMarker(Mission mission) {
        Integer agvId = mission.getAgv().getAgvId();

        if (agvId == 1) {
            return resolveZoneMarker("AGV01_START");
        }

        if (agvId == 2) {
            return resolveZoneMarker("AGV02_START");
        }

        throw new IllegalArgumentException("지원하지 않는 AGV ID입니다: " + agvId);
    }
}
