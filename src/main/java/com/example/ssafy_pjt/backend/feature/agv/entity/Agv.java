package com.example.ssafy_pjt.backend.feature.agv.entity;

import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agv")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Agv {

    @Id
    @Column(name = "agv_id", length = 50)
    private String agvId;

    @Column(nullable = false)
    private Integer battery;

    @Column(name = "current_speed")
    private Double currentSpeed;

    @Column(name = "carrying_item")
    private Boolean carryingItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgvStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_zone_id")
    private Zone currentZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_marker_id")
    private ArucoMarker currentMarker;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}
