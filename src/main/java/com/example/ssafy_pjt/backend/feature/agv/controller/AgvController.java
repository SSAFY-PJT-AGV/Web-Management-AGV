package com.example.ssafy_pjt.backend.feature.agv.controller;

import com.example.ssafy_pjt.backend.feature.agv.dto.AgvResponse;
import com.example.ssafy_pjt.backend.feature.agv.service.AgvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agvs")
@RequiredArgsConstructor
public class AgvController {
    private final AgvService agvService;

    @GetMapping
    public List<AgvResponse> getAgvs() {
        return agvService.getAgvs();
    }
}