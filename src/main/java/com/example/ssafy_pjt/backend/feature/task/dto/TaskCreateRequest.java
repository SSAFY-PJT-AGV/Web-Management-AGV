package com.example.ssafy_pjt.backend.feature.task.dto;

import com.example.ssafy_pjt.backend.feature.product.enums.ProductType;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskPriority;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateRequest {
    private TaskType taskType;
    private ProductType productType;
    private Integer quantity;
    private TaskPriority priority;
}
