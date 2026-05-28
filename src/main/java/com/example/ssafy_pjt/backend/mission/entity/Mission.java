package com.example.ssafy_pjt.backend.mission.entity;

import com.example.ssafy_pjt.backend.agv.entity.Agv;
import com.example.ssafy_pjt.backend.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.material.entity.Material;
import com.example.ssafy_pjt.backend.task.entity.ProductionTask;
import com.example.ssafy_pjt.backend.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ProductionTask productionTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agv_id")
    private Agv agv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false, length = 30)
    private MissionType missionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_zone_id")
    private Zone sourceZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_zone_id")
    private Zone targetZone;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void assignAgv(Agv agv) {
        this.agv = agv;
        this.status = MissionStatus.ASSIGNED;
    }

    public void start() {
        this.status = MissionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = MissionStatus.FAILED;
        this.failureReason = reason;
    }
}
