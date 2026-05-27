package com.example.ssafy_pjt.backend.productiontask.entity;

import com.example.ssafy_pjt.backend.productiontask.enums.ProductType;
import com.example.ssafy_pjt.backend.productiontask.enums.TaskPriority;
import com.example.ssafy_pjt.backend.productiontask.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 50)
    private ProductType productType;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    private  LocalDateTime updatedAt;


    public void changeStatus(TaskStatus status){
        this.status = status;
        this.updatedAt = LocalDateTime.now();

        if(status == TaskStatus.IN_PROGRESS){
            this.startedAt = LocalDateTime.now();
        }

        if(status == TaskStatus.COMPLETED){
            this.completedAt = LocalDateTime.now();
        }
    }
}
