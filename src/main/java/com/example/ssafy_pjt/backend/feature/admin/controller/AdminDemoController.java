package com.example.ssafy_pjt.backend.feature.admin.controller;

import com.example.ssafy_pjt.backend.feature.admin.dto.DemoActionRequest;
import com.example.ssafy_pjt.backend.feature.admin.service.AdminDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/demo")
@RequiredArgsConstructor
public class AdminDemoController {

    private final AdminDemoService adminDemoService;

    @PostMapping("/reset")
    public void reset(@RequestBody DemoActionRequest request) {
        adminDemoService.resetDemoState(request);
    }

    @PostMapping("/agvs/{agvId}/connect")
    public void connectFakeAgv(
            @PathVariable Integer agvId,
            @RequestBody DemoActionRequest request
    ) {
        adminDemoService.connectFakeAgv(agvId, request);
    }
}
