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

        Integer markerId = switch (type) {
            case PICK_FROM_STORAGE -> resolveZoneMarker("MATERIAL_BOX_STORAGE");
            case DROP_TO_CONVEYOR -> resolveZoneMarker("CONVEYOR_START");
            case PICK_FROM_CONVEYOR -> resolveZoneMarker("CONVEYOR_END");

            case DROP_TO_FINISHED_BOX_STORAGE,
                 PICK_FROM_FINISHED_BOX_STORAGE -> resolveZoneMarker("FINISHED_BOX_STORAGE");

            case PICK_FROM_INBOUND -> resolveZoneMarker("INBOUND");
            case DROP_TO_OUTBOUND -> resolveZoneMarker("OUTBOUND");

            case PICK_FROM_CROSS,
                 DROP_TO_CROSS -> resolveZoneMarker("CROSS_ZONE");

            case PICK_EMPTY_BOX -> resolveZoneMarker("MATERIAL_BOX_STORAGE");

            case DROP_EMPTY_BOX -> {
                if (mission.getProduct() != null) {
                    yield resolveZoneMarker("FINISHED_BOX_STORAGE");
                }
                yield resolveZoneMarker("OUTBOUND");
            }

            case DROP_TO_STORAGE -> resolveZoneMarker("MATERIAL_BOX_STORAGE");
            case RETURN_TO_BASE -> resolveReturnBaseMarker(mission);

            default -> throw new IllegalArgumentException(
                    "지원하지 않는 MissionType: " + type
            );
        };

        System.out.println(
                "[DESTINATION RESOLVE]"
                        + " missionId=" + mission.getMissionId()
                        + ", type=" + type
                        + ", markerId=" + markerId
        );

        return markerId;
    }

    public Integer resolveCargoMarkerId(Mission mission) {
        MissionType type = mission.getMissionType();

        Integer markerId = switch (type) {
            case PICK_FROM_STORAGE,
                 DROP_TO_CONVEYOR,
                 PICK_FROM_CONVEYOR,
                 DROP_TO_FINISHED_BOX_STORAGE,
                 PICK_FROM_INBOUND,
                 PICK_FROM_CROSS,
                 DROP_TO_CROSS,
                 DROP_TO_STORAGE -> resolveMaterialMarker(mission);

            case PICK_FROM_FINISHED_BOX_STORAGE,
                 DROP_TO_OUTBOUND -> resolveProductBoxMarker(mission, false);

            case PICK_EMPTY_BOX -> {
                if (mission.getProduct() != null) {
                    yield resolveProductBoxMarker(mission, true);
                }
                yield resolveMaterialBoxMarker(mission, true);
            }

            case DROP_EMPTY_BOX -> {
                if (mission.getProduct() != null) {
                    yield resolveProductBoxMarkerIgnoreEmptyStatus(mission);
                }
                yield resolveMaterialBoxMarkerIgnoreEmptyStatus(mission);
            }

            default -> null;
        };

        System.out.println(
                "[CARGO RESOLVE]"
                        + " missionId=" + mission.getMissionId()
                        + ", type=" + type
                        + ", markerId=" + markerId
        );

        return markerId;
    }

    private Integer resolveProductBoxMarker(Mission mission, Boolean emptyStatus) {
        if (mission.getProduct() == null) {
            throw new IllegalStateException(
                    "완제품 박스 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", product=null"
            );
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductIdAndEmptyStatus(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId(),
                        emptyStatus
                )
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "완제품 박스 마커 없음 productId="
                                        + mission.getProduct().getProductId()
                                        + ", emptyStatus="
                                        + emptyStatus
                        ))
                .getMarkerId();
    }

    private Integer resolveProductBoxMarkerIgnoreEmptyStatus(Mission mission) {
        if (mission.getProduct() == null) {
            throw new IllegalStateException(
                    "완제품 박스 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", product=null"
            );
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndProduct_ProductIdAndEmptyStatus(
                        MarkerType.PRODUCT_TYPE,
                        mission.getProduct().getProductId(),
                        true
                )
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "완제품 박스 마커 없음 productId="
                                        + mission.getProduct().getProductId()
                                        + ", emptyStatus=true"
                        ))
                .getMarkerId();
    }

    private Integer resolveMaterialMarker(Mission mission) {
        if (mission.getMaterial() == null) {
            throw new IllegalStateException(
                    "자재 타입 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", material=null"
            );
        }

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

    private Integer resolveMaterialBoxMarker(Mission mission, Boolean emptyStatus) {
        if (mission.getMaterial() == null) {
            throw new IllegalStateException(
                    "자재 박스 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", material=null"
            );
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialIdAndEmptyStatus(
                        MarkerType.MATERIAL_BOX,
                        mission.getMaterial().getMaterialId(),
                        emptyStatus
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "자재 박스 마커 없음 materialId="
                                        + mission.getMaterial().getMaterialId()
                                        + ", emptyStatus="
                                        + emptyStatus
                        ))
                .getMarkerId();
    }

    private Integer resolveMaterialBoxMarkerIgnoreEmptyStatus(Mission mission) {
        if (mission.getMaterial() == null) {
            throw new IllegalStateException(
                    "자재 박스 마커를 찾을 수 없습니다. missionId="
                            + mission.getMissionId()
                            + ", material=null"
            );
        }

        return arucoMarkerRepository
                .findByMarkerTypeAndMaterial_MaterialId(
                        MarkerType.MATERIAL_BOX,
                        mission.getMaterial().getMaterialId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "자재 박스 마커 없음 materialId="
                                        + mission.getMaterial().getMaterialId()
                        ))
                .getMarkerId();
    }

    private Integer resolveZoneMarker(String zoneName) {
        String searchZoneName;

        if ("OUTBOUND".equals(zoneName)) {
            searchZoneName = "INBOUND";
        } else {
            searchZoneName = zoneName;
        }

        Integer markerId = arucoMarkerRepository
                .findByMarkerTypeAndZone_ZoneName(
                        MarkerType.ZONE,
                        searchZoneName
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Zone marker 없음: request="
                                        + zoneName
                                        + ", search="
                                        + searchZoneName
                        ))
                .getMarkerId();

        System.out.println(
                "[ZONE RESOLVE] request="
                        + zoneName
                        + ", search="
                        + searchZoneName
                        + ", markerId="
                        + markerId
        );

        return markerId;
    }

    private Integer resolveReturnBaseMarker(Mission mission) {
        Integer agvId = mission.getAgv().getAgvId();

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