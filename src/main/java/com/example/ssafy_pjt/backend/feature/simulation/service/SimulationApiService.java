package com.example.ssafy_pjt.backend.feature.simulation.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import com.example.ssafy_pjt.backend.feature.marker.service.MarkerResolveService;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
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
        if (agv.getCargoMaterial() != null) {
            return agv.getCargoMaterial().getMaterialCode();
        }

        if (mission != null && mission.getMaterial() != null) {
            return mission.getMaterial().getMaterialCode();
        }

        if (mission != null && mission.getProduct() != null) {
            return mission.getProduct().getProductType().name();
        }

        return null;
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