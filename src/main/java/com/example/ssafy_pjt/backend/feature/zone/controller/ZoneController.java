package com.example.ssafy_pjt.backend.feature.zone.controller;

import com.example.ssafy_pjt.backend.feature.zone.dto.ZoneResponse;
import com.example.ssafy_pjt.backend.feature.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneService zoneService;

    @GetMapping
    public List<ZoneResponse> getZones() {
        return zoneService.getZones();
    }
}
