package com.example.ssafy_pjt.backend.feature.task.controller;

import com.example.ssafy_pjt.backend.feature.task.dto.TaskCreateRequest;
import com.example.ssafy_pjt.backend.feature.task.dto.TaskResponse;
import com.example.ssafy_pjt.backend.feature.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService productionTaskService;

    @PostMapping
    public TaskResponse createTask(
            @RequestBody TaskCreateRequest request
    ) {
        return productionTaskService.createTask(request);
    }

    @GetMapping
    public List<TaskResponse> getTasks() {
        return productionTaskService.getTasks();
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTask(
            @PathVariable Long taskId
    ) {
        return productionTaskService.getTask(taskId);
    }
}
