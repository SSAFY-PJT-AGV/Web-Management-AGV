package com.example.ssafy_pjt.backend.feature.agv.controller;

import com.example.ssafy_pjt.backend.feature.agv.service.ChipScenarioTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/scenario")
@RequiredArgsConstructor
public class ScenarioTestController {

    private final ChipScenarioTestService chipScenarioTestService;

    @PostMapping
    public String startChipScenario() {
        chipScenarioTestService.startScenario();
        return "CHIP → CAR_CONTROL_UNIT 시나리오 시작";
    }

    @PostMapping("/reset")
    public String resetChipScenario() {
        chipScenarioTestService.resetScenario();
        return "CHIP → CAR_CONTROL_UNIT 시나리오 초기화";
    }
}