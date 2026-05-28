package com.example.ssafy_pjt.backend.reservation.entity;

import com.example.ssafy_pjt.backend.agv.entity.Agv;
import com.example.ssafy_pjt.backend.mission.entity.Mission;
import com.example.ssafy_pjt.backend.reservation.enums.ReservationStatus;
import com.example.ssafy_pjt.backend.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agv_id", nullable = false)
    private Agv agv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "occupied_at")
    private LocalDateTime occupiedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;
}
