package com.example.ssafy_pjt.backend.feature.task.controller;

import com.example.ssafy_pjt.backend.feature.mission.service.ReplenishmentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replenishments")
@RequiredArgsConstructor
public class ReplenishmentController {
    private final ReplenishmentService replenishmentService;

    @PostMapping
    public ResponseEntity<Void> createReplenishment(
            @RequestBody ReplenishmentRequest request
    ) {
        replenishmentService.createReplenishmentMissions(
                request.getMaterialCode(),
                request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }

    @Getter
    public static class ReplenishmentRequest {
        private String materialCode;
        private int quantity;
    }
}
