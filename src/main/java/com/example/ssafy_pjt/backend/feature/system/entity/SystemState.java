package com.example.ssafy_pjt.backend.feature.system.entity;

import com.example.ssafy_pjt.backend.feature.system.enums.OperationMode;
import com.example.ssafy_pjt.backend.feature.system.enums.ScenarioStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemState {

    @Id
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationMode mode;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScenarioStatus scenarioStatus;


    @Column(nullable = false)
    private String activeLine;


    private LocalDateTime lastResetAt;


    private LocalDateTime updatedAt;
}