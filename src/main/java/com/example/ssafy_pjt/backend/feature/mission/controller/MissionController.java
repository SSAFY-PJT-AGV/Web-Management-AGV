package com.example.ssafy_pjt.backend.feature.mission.controller;

import com.example.ssafy_pjt.backend.feature.mission.dto.MissionResponse;
import com.example.ssafy_pjt.backend.feature.mission.dto.MissionSummaryResponse;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {
    private final MissionService missionService;

    @GetMapping
    public List<MissionResponse> getMissions() {
        return missionService.getMissions();
    }

    @GetMapping("/summary")
    public List<MissionSummaryResponse> getMissionSummary() {
        return missionService.getMissionSummary();
    }
}
