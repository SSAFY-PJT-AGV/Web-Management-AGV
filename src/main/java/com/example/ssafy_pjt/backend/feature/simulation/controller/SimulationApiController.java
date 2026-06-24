package com.example.ssafy_pjt.backend.feature.simulation.controller;

import com.example.ssafy_pjt.backend.feature.simulation.dto.SimulationAgvResponse;
import com.example.ssafy_pjt.backend.feature.simulation.service.SimulationApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class SimulationApiController {

    private final SimulationApiService simulationApiService;


    @GetMapping("/agvs")
    public SimulationAgvResponse getAgvSimulationState() {
        return simulationApiService.getAgvSimulationState();
    }


    @PostMapping("/open-gazebo")
    public void openGazebo() {
        simulationApiService.openGazebo();
    }
}