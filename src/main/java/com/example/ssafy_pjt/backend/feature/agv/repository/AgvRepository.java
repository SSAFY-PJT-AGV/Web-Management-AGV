package com.example.ssafy_pjt.backend.feature.agv.repository;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvRepository extends JpaRepository<Agv, Integer> {
}