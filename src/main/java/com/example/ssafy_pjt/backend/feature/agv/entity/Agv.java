package com.example.ssafy_pjt.backend.feature.agv.entity;

import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.enums.CargoType;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "agv")
@Getter
@Setter
@NoArgsConstructor
public class Agv {

    @Id
    @Column(name = "agv_id", length = 50)
    private String agvId;

    @Column(nullable = false)
    private Integer battery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgvStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_marker_id")
    private ArucoMarker currentMarker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_mission_id")
    private Mission currentMission;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo_type", nullable = false, length = 30)
    private CargoType cargoType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_material_id")
    private Material cargoMaterial;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}