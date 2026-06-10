package com.example.ssafy_pjt.backend.feature.marker.repository;

import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArucoMarkerRepository extends JpaRepository<ArucoMarker, Integer> {

    Optional<ArucoMarker> findByZone_ZoneId(Long zoneId);

    Optional<ArucoMarker> findByZone_ZoneName(String zoneName);

    Optional<ArucoMarker> findByMarkerTypeAndMaterial_MaterialId(
            MarkerType markerType,
            Long materialId
    );

    Optional<ArucoMarker> findByMarkerTypeAndZone_ZoneName(
            MarkerType markerType,
            String zoneName
    );

    // PRODUCT_TYPE + product_id → marker 11/12/13
    Optional<ArucoMarker> findByMarkerTypeAndProduct_ProductId(
            MarkerType markerType,
            Long productId
    );
}