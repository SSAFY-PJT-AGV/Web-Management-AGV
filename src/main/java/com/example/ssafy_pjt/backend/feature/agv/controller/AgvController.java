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

    @PostMapping("/{agvId}/pause")
    public void pauseAgv(@PathVariable Integer agvId) {
        agvService.pauseAgv(agvId);
    }

    @PostMapping("/{agvId}/resume")
    public void resumeAgv(@PathVariable Integer agvId) {
        agvService.resumeAgv(agvId);
    }

    @PostMapping("/{agvId}/cancel-current-task")
    public void cancelCurrentTask(@PathVariable Integer agvId) {
        agvService.cancelCurrentTask(agvId);
    }
}