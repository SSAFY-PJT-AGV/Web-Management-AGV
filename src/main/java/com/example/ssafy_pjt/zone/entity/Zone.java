package com.example.ssafy_pjt.zone.entity;

import com.example.ssafy_pjt.agv.entity.Agv;
import com.example.ssafy_pjt.zone.enums.ZoneStatus;
import com.example.ssafy_pjt.zone.enums.ZoneType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zone")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "zone_name", nullable = false, length = 50)
    private String zoneName;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false, length = 30)
    private ZoneType zoneType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ZoneStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_agv_id")
    private Agv currentAgv;
}
