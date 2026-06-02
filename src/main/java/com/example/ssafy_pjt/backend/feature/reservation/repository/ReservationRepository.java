package com.example.ssafy_pjt.backend.feature.reservation.repository;

import com.example.ssafy_pjt.backend.feature.reservation.entity.Reservation;
import com.example.ssafy_pjt.backend.feature.reservation.enums.ReservationStatus;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByZoneAndStatusIn(Zone zone, List<ReservationStatus> statuses);
}