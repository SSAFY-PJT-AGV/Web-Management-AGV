package com.example.ssafy_pjt.backend.feature.simulation.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.marker.service.MarkerResolveService;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.simulation.dto.SimulationAgvResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SimulationApiService {

    private final AgvRepository agvRepository;
    private final ArucoMarkerRepository arucoMarkerRepository;
    private final MarkerResolveService markerResolveService;

    @Transactional(readOnly = true)
    public SimulationAgvResponse getAgvSimulationState() {
        var agvs = agvRepository.findAll()
                .stream()
                .map(this::toSimulationItem)
                .toList();

        return new SimulationAgvResponse(agvs);
    }

    private SimulationAgvResponse.SimulationAgvItem toSimulationItem(Agv agv) {
        Mission mission = agv.getCurrentMission();

        ArucoMarker currentMarker = agv.getCurrentMarker();

        Integer nextMarkerId = resolveNextMarkerId(mission);

        ArucoMarker nextMarker = nextMarkerId == null
                ? null
                : arucoMarkerRepository.findById(nextMarkerId).orElse(null);

        return new SimulationAgvResponse.SimulationAgvItem(
                agv.getAgvId(),

                mission == null || mission.getTask() == null
                        ? null
                        : mission.getTask().getTaskId(),

                agv.getStatus().name(),

                mission == null || mission.getSourceZone() == null
                        ? null
                        : mission.getSourceZone().getZoneName(),

                mission == null || mission.getTargetZone() == null
                        ? null
                        : mission.getTargetZone().getZoneName(),

                currentMarker == null
                        ? null
                        : currentMarker.getMarkerId(),

                nextMarkerId,

                resolvePosition(currentMarker),

                resolvePosition(nextMarker),

                agv.getLastSeenAt() == null
                        ? LocalDateTime.now()
                        : agv.getLastSeenAt(),

                resolvePayload(agv, mission),

                resolveDebug(
                        agv,
                        mission,
                        nextMarkerId
                )
        );
    }

    private Integer resolveNextMarkerId(Mission mission) {
        if (mission == null) {
            return null;
        }

        try {
            return markerResolveService.resolveDestinationMarkerId(mission);
        } catch (Exception e) {
            return null;
        }
    }

    private SimulationAgvResponse.Position resolvePosition(ArucoMarker marker) {
        if (marker == null) {
            return null;
        }

        if (marker.getXPosition() != null && marker.getYPosition() != null) {
            return new SimulationAgvResponse.Position(
                    marker.getXPosition(),
                    marker.getYPosition()
            );
        }

        if (marker.getZone() == null) {
            return null;
        }

        ArucoMarker zoneMarker = arucoMarkerRepository
                .findByMarkerTypeAndZone_ZoneName(
                        MarkerType.ZONE,
                        marker.getZone().getZoneName()
                )
                .orElse(null);

        if (zoneMarker == null) {
            return null;
        }

        return new SimulationAgvResponse.Position(
                zoneMarker.getXPosition(),
                zoneMarker.getYPosition()
        );
    }

    private String resolvePayload(Agv agv, Mission mission) {

        String actualCargo = resolveActualCargo(agv);

        if (actualCargo != null) {
            return actualCargo;
        }

        if (mission == null) {
            return null;
        }

        return switch (mission.getMissionType()) {

            case PICK_FROM_STORAGE,
                 DROP_TO_CONVEYOR -> {
                if (mission.getMaterial() == null) {
                    yield null;
                }

                yield mission.getMaterial().getMaterialCode();
            }

            case PICK_FROM_INBOUND -> {
                if (mission.getMaterial() == null) {
                    yield null;
                }

                yield "REPLENISHMENT_BOX:" + mission.getMaterial().getMaterialCode();
            }

            case DROP_TO_CROSS -> {
                if (agv.getAgvId() == 1) {
                    yield "EMPTY_BOX";
                }

                if (mission.getMaterial() == null) {
                    yield null;
                }

                yield "REPLENISHMENT_BOX:" + mission.getMaterial().getMaterialCode();
            }

            case PICK_FROM_CROSS -> {
                if (agv.getAgvId() == 1) {
                    if (mission.getMaterial() == null) {
                        yield null;
                    }

                    yield "REPLENISHMENT_BOX:" + mission.getMaterial().getMaterialCode();
                }

                yield "EMPTY_BOX";
            }

            case DROP_TO_STORAGE -> {
                if (mission.getMaterial() == null) {
                    yield null;
                }

                yield "REPLENISHMENT_BOX:" + mission.getMaterial().getMaterialCode();
            }

            case PICK_FROM_CONVEYOR,
                 DROP_TO_FINISHED_BOX_STORAGE -> {
                if (mission.getMaterial() == null) {
                    yield null;
                }

                yield mission.getMaterial().getMaterialCode();
            }

            case PICK_FROM_FINISHED_BOX_STORAGE,
                 DROP_TO_OUTBOUND -> {
                if (mission.getProduct() == null) {
                    yield null;
                }

                yield "PRODUCT_BOX:" + mission.getProduct().getProductType().name();
            }

            case PICK_EMPTY_BOX,
                 DROP_EMPTY_BOX -> "EMPTY_BOX";

            default -> null;
        };
    }

    private String resolveActualCargo(Agv agv) {
        if (agv == null) {
            return null;
        }

        if (agv.getCargoType() == null) {
            return null;
        }

        return switch (agv.getCargoType()) {

            case NONE -> null;

            case EMPTY_BOX -> "EMPTY_BOX";

            case MATERIAL -> agv.getCargoMaterial() == null
                    ? null
                    : agv.getCargoMaterial().getMaterialCode();

            case FINISHED_BOX -> "PRODUCT_BOX";
        };
    }

    private String resolveDebug(
            Agv agv,
            Mission mission,
            Integer nextMarkerId
    ) {

        if (agv.getCurrentMarker() == null) {
            return "AGV_LOCATION_UNKNOWN";
        }

        if (mission == null) {
            return "NO_ACTIVE_MISSION";
        }

        if (nextMarkerId == null) {
            return "DESTINATION_RESOLVE_FAILED";
        }

        return "OK";
    }
}