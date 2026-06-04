package com.example.ssafy_pjt.backend.feature.material.service;

import com.example.ssafy_pjt.backend.feature.material.dto.MaterialResponse;
import com.example.ssafy_pjt.backend.feature.material.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterials() {
        return materialRepository.findAll()
                .stream()
                .map(MaterialResponse::new)
                .toList();
    }
}