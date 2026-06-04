package com.example.ssafy_pjt.backend.feature.mission.controller;

import com.example.ssafy_pjt.backend.feature.mission.dto.MissionResponse;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final MissionDispatchService missionDispatchService;

    @PostMapping("/{agvId}")
    public ResponseEntity<MissionResponse> dispatchNextMission(
            @PathVariable Integer agvId
    ) {
        Mission mission = missionDispatchService.dispatchNextMission(agvId);
        return ResponseEntity.ok(new MissionResponse(mission));
    }
}