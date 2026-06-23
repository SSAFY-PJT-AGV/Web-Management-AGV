package com.example.ssafy_pjt.backend.feature.task.repository;

import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {

    List<ProductionTask> findByStatusIn(List<TaskStatus> statuses);
}