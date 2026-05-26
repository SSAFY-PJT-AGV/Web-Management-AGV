package com.example.ssafy_pjt.marker.entity;

import com.example.ssafy_pjt.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "aruco_marker")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ArucoMarker {

    @Id
    @Column(name = "marker_id")
    private Integer markerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "x_position", nullable = false)
    private Double xPosition;

    @Column(name = "y_position", nullable = false)
    private Double yPosition;

    @Column(length = 100)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;
}