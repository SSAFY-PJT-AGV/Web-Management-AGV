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
                    resolveZoneMarker("MATERIAL_BOX_STORAGE");

            case DROP_TO_CONVEYOR ->
                    resolveZoneMarker("CONVEYOR_START");

            case PICK_FROM_CONVEYOR ->
                    resolveZoneMarker("CONVEYOR_END");

            case DROP_TO_FINISHED_BOX_STORAGE,
                 PICK_FROM_FINISHED_BOX_STORAGE ->
                    resolveZoneMarker("FINISHED_BOX_STORAGE");

            case PICK_FROM_INBOUND ->
                    resolveZoneMarker("INBOUND");

            case DROP_TO_OUTBOUND ->
                    resolveZoneMarker("OUTBOUND");

            case PICK_FROM_CROSS,
                 DROP_TO_CROSS ->
                    resolveZoneMarker("CROSS_ZONE");

            case PICK_EMPTY_BOX ->
                    resolveZoneMarker("MATERIAL_BOX_STORAGE");

            case DROP_EMPTY_BOX -> {
                if (mission.getProduct() != null) {
                    yield resolveZoneMarker("FINISHED_BOX_STORAGE");
                }

                yield resolveZoneMarker("OUTBOUND");
            }

            case DROP_TO_STORAGE ->
                    resolveZoneMarker("MATERIAL_BOX_STORAGE");

            case RETURN_TO_BASE ->
                    resolveReturnBaseMarker(mission);

            default ->
                    throw new IllegalArgumentException(
                            "지원하지 않는 MissionType: " + type
                    );
        };
    }


    public Integer resolveCargoMarkerId(Mission mission) {

        MissionType type = mission.getMissionType();

        return switch (type) {

            // 부품 자체
            case PICK_FROM_STORAGE,
                 DROP_TO_CONVEYOR,
                 PICK_FROM_CONVEYOR,
                 DROP_TO_FINISHED_BOX_STORAGE ->
                    resolveMaterialMarker(mission);

            // 자재 상자
            case PICK_FROM_INBOUND,
                 PICK_FROM_CROSS,
                 DROP_TO_CROSS,
                 DROP_TO_STORAGE ->
                    resolveMaterialBoxMarker(mission, false);

            // 완제품 상자
            case PICK_FROM_FINISHED_BOX_STORAGE,
                 DROP_TO_OUTBOUND ->
                    resolveProductBoxMarker(
                            mission,
                            false
                    );

            // 빈 상자
            case PICK_EMPTY_BOX,
                 DROP_EMPTY_BOX -> {
                if (mission.getProduct() != null) {
                    yield resolveProductBoxMarker(mission, true);
                }

                yield resolveMaterialBoxMarker(mission, true);
            }

            default ->
                    null;
        };
    }

    private Integer resolveProductBoxMarker(Mission mission, Boolean isEmpty) {

        if (mission.getProduct() == null) {
            throw new IllegalStateException(
                    "완제품 박스 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", product=null"
            );
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductIdAndEmpty(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId(),
                        isEmpty
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "완제품 박스 마커 없음 productId="
                                        + mission.getProduct().getProductId()
                                        + ", isEmpty="
                                        + isEmpty
                        ))
                .getMarkerId();
    }


    private Integer resolveMaterialMarker(Mission mission) {

        return arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialId(
                        MarkerType.MATERIAL_TYPE,
                        mission.getMaterial().getMaterialId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "자재 타입 마커 없음 materialId="
                                        + mission.getMaterial().getMaterialId()
                        ))
                .getMarkerId();
    }


    private Integer resolveMaterialBoxMarker(Mission mission, Boolean isEmpty) {

        if (mission.getMaterial() == null) {
            throw new IllegalStateException(
                    "자재 박스 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", material=null"
            );
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialIdAndEmpty(
                        MarkerType.MATERIAL_BOX,
                        mission.getMaterial().getMaterialId(),
                        isEmpty
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "자재 박스 마커 없음 materialId="
                                        + mission.getMaterial().getMaterialId()
                                        + ", isEmpty="
                                        + isEmpty
                        ))
                .getMarkerId();
    }


    private Integer resolveProductMarker(Mission mission) {

        return arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductId(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "완제품 마커 없음 productId="
                                        + mission.getProduct().getProductId()
                        ))
                .getMarkerId();
    }


    private Integer resolveZoneMarker(String zoneName) {

        return arucoMarkerRepository
                .findByMarkerTypeAndZone_ZoneName(
                        MarkerType.ZONE,
                        zoneName
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Zone marker 없음: " + zoneName
                        ))
                .getMarkerId();
    }


    private Integer resolveReturnBaseMarker(Mission mission) {

        Integer agvId =
                mission.getAgv().getAgvId();

        if (agvId == 1) {
            return resolveZoneMarker("AGV01_START");
        }

        if (agvId == 2) {
            return resolveZoneMarker("FINISHED_BOX_STORAGE");
        }

        throw new IllegalArgumentException(
                "지원하지 않는 AGV ID=" + agvId
        );
    }
}