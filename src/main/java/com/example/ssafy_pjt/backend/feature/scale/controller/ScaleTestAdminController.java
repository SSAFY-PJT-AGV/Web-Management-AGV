package com.example.ssafy_pjt.backend.feature.scale.controller;


import com.example.ssafy_pjt.backend.feature.scale.dto.ScaleTestPrepareRequest;
import com.example.ssafy_pjt.backend.feature.scale.service.ScaleTestAdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/scale-test")
public class ScaleTestAdminController {


    private final ScaleTestAdminService service;


    @PostMapping("/prepare")
    public void prepare(
            @RequestBody ScaleTestPrepareRequest request
    ) {

        service.prepare(
                request.supplyCount(),
                request.collectCount()
        );
    }


    @PostMapping("/cleanup")
    public void cleanup() {

        service.cleanup();

    }
}