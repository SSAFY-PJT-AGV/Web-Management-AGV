package com.example.ssafy_pjt.backend.feature.task.dto;

import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskResponse {
    private final Long taskId;
    private final String productType;
    private final Integer quantity;
    private final String priority;
    private final String status;
    private final LocalDateTime createdAt;

    public TaskResponse(ProductionTask task) {
        this.taskId = task.getTaskId();
        this.productType = task.getProductType().name();
        this.quantity = task.getQuantity();
        this.priority = task.getPriority().name();
        this.status = task.getStatus().name();
        this.createdAt = task.getCreatedAt();
    }
}
