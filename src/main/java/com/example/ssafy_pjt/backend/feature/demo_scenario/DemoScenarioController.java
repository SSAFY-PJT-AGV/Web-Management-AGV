package com.example.ssafy_pjt.backend.feature.demo_scenario;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/demo/scenario")
@RequiredArgsConstructor
public class DemoScenarioController {

    private final DemoScenarioService demoScenarioService;

    @PostMapping("/start")
    public void start() {
        demoScenarioService.start();
    }

    @PostMapping("/stop")
    public void stop() {
        demoScenarioService.stop();
    }

    @PostMapping("/reset")
    public void reset() {
        demoScenarioService.reset();
    }
}