package com.example.ssafy_pjt.backend.feature.simulation.controller;

import com.example.ssafy_pjt.backend.feature.simulation.dto.SimulationAgvResponse;
import com.example.ssafy_pjt.backend.feature.simulation.service.SimulationApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SimulationApiController {

    private final SimulationApiService simulationApiService;

    @GetMapping("/api/simulation/agvs")
    public SimulationAgvResponse getAgvSimulationState() {
        return simulationApiService.getAgvSimulationState();
    }
}