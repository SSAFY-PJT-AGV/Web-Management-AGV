package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.marker.dto.AgvMapItem;
import com.example.ssafy_pjt.backend.feature.marker.dto.FactoryMapResponse;
import com.example.ssafy_pjt.backend.feature.marker.dto.MarkerMapItem;
import com.example.ssafy_pjt.backend.feature.marker.repository.ArucoMarkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarkerMapService {

    private final ArucoMarkerRepository arucoMarkerRepository;
    private final AgvRepository agvRepository;

    @Transactional(readOnly = true)
    public FactoryMapResponse getFactoryMap() {
        List<MarkerMapItem> markers = arucoMarkerRepository.findAll()
                .stream()
                .map(marker -> new MarkerMapItem(
                        marker.getMarkerId(),
                        marker.getXPosition(),
                        marker.getYPosition(),
                        marker.getZone() == null ? null : marker.getZone().getZoneName(),
                        marker.getMarkerType().name()
                ))
                .toList();

        List<AgvMapItem> agvs = agvRepository.findAll()
                .stream()
                .map(agv -> new AgvMapItem(
                        "AGV%02d".formatted(agv.getAgvId()),
                        agv.getCurrentMarker() == null ? null : agv.getCurrentMarker().getMarkerId(),
                        agv.getStatus().name()
                ))
                .toList();

        return new FactoryMapResponse(markers, agvs);
    }
}