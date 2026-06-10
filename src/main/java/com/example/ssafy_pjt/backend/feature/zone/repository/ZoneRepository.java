package com.example.ssafy_pjt.backend.feature.zone.repository;

import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Optional<Zone> findByZoneName(String zoneName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select z from Zone z where z.zoneName = :zoneName")
    Optional<Zone> findByZoneNameForUpdate(String zoneName);
}