package com.example.ssafy_pjt.backend.feature.marker.controller;

import com.example.ssafy_pjt.backend.feature.marker.dto.FactoryMapResponse;
import com.example.ssafy_pjt.backend.feature.marker.service.ArucoService;
import com.example.ssafy_pjt.backend.feature.marker.service.MarkerMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/markers")
@RequiredArgsConstructor
public class MarkerController {

    private final MarkerMapService markerMapService;

    @GetMapping("/map")
    public FactoryMapResponse getFactoryMap() {
        return markerMapService.getFactoryMap();
    }
}