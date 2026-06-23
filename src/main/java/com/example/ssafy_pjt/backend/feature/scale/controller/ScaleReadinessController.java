package com.example.ssafy_pjt.backend.feature.scale.controller;

import com.example.ssafy_pjt.backend.feature.scale.service.ScaleReadinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scale")
public class ScaleReadinessController {

    private final ScaleReadinessService service;


    @GetMapping("/analyze")
    public String analyze(
            @RequestParam int agv,
            @RequestParam int fps
    ) {

        return service.analyze(
                agv,
                fps
        );
    }
}