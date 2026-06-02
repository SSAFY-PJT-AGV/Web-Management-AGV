package com.example.ssafy_pjt.backend.feature.zone.repository;

import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByZoneName(String zoneName);
}