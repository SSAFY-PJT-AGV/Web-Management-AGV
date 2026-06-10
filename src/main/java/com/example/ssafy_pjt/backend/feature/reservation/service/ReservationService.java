package com.example.ssafy_pjt.backend.feature.reservation.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.reservation.entity.Reservation;
import com.example.ssafy_pjt.backend.feature.reservation.enums.ReservationStatus;
import com.example.ssafy_pjt.backend.feature.reservation.repository.ReservationRepository;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import com.example.ssafy_pjt.backend.feature.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String CROSS_ZONE_NAME = "CROSS_ZONE";

    private final ReservationRepository reservationRepository;
    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public boolean isCrossZoneAvailable() {
        Zone crossZone = getCrossZone();

        return reservationRepository.findByZoneAndStatusIn(
                crossZone,
                List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
        ).isEmpty();
    }

    @Transactional
    public void reserveCrossZone(Agv agv, Mission mission) {
        if (!isCrossZoneAvailable()) {
            throw new IllegalStateException("교차구역을 예약할 수 없습니다.");
        }

        Reservation reservation = new Reservation();
        reservation.setZone(getCrossZone());
        reservation.setAgv(agv);
        reservation.setMission(mission);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setReservedAt(LocalDateTime.now());

        reservationRepository.save(reservation);
    }

    @Transactional
    public void occupyCrossZone(Agv agv, Mission mission) {
        Reservation reservation = findActiveReservation(agv, mission);

        reservation.setStatus(ReservationStatus.OCCUPIED);
        reservation.setOccupiedAt(LocalDateTime.now());
    }

    @Transactional
    public void releaseCrossZone(Agv agv, Mission mission) {
        Reservation reservation = findActiveReservation(agv, mission);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setReleasedAt(LocalDateTime.now());
    }

    private Reservation findActiveReservation(Agv agv, Mission mission) {
        Zone crossZone = getCrossZone();

        return reservationRepository.findByZoneAndStatusIn(
                        crossZone,
                        List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
                )
                .stream()
                .filter(reservation -> reservation.getAgv().getAgvId().equals(agv.getAgvId()))
                .filter(reservation -> reservation.getMission().getMissionId().equals(mission.getMissionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("활성 교차구역 예약이 없습니다."));
    }

    private Zone getCrossZone() {
        return zoneRepository.findByZoneName(CROSS_ZONE_NAME)
                .orElseThrow(() -> new IllegalArgumentException("교차구역 Zone이 없습니다."));
    }
}