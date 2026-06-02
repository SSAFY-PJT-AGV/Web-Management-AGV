package com.example.ssafy_pjt.backend.feature.task.repository;

import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {
}