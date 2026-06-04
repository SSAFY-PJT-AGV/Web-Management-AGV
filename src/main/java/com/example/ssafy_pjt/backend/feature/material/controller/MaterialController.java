package com.example.ssafy_pjt.backend.feature.material.controller;

import com.example.ssafy_pjt.backend.feature.material.dto.MaterialResponse;
import com.example.ssafy_pjt.backend.feature.material.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public List<MaterialResponse> getMaterials() {
        return materialService.getMaterials();
    }
}