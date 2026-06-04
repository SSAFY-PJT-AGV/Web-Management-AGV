package com.example.ssafy_pjt.backend.feature.task.dto;

import com.example.ssafy_pjt.backend.feature.task.enums.ProductType;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskPriority;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateRequest {
    private ProductType productType;
    private Integer quantity;
    private TaskPriority priority;
}
