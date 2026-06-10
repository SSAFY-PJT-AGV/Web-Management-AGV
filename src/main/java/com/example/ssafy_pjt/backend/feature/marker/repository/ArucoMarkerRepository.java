package com.example.ssafy_pjt.backend.feature.marker.repository;

import com.example.ssafy_pjt.backend.feature.marker.entity.ArucoMarker;
import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArucoMarkerRepository extends JpaRepository<ArucoMarker, Integer> {

    // Zone → Marker 찾기
    Optional<ArucoMarker> findByZone_ZoneId(Long zoneId);

    Optional<ArucoMarker> findByZone_ZoneName(String zoneName);


    // 자재 박스 찾기
    // MATERIAL_BOX + CHIP → marker 3
    // MATERIAL_BOX + SENSOR → marker 4
    // MATERIAL_BOX + BATTERY → marker 5
    Optional<ArucoMarker> findByMarkerTypeAndMaterial_MaterialId(
            MarkerType markerType,
            Long materialId
    );


    // 특정 타입 + Zone 찾기
    // PRODUCT_TYPE + FINISHED_BOX_STORAGE → 11,12,13
    Optional<ArucoMarker> findByMarkerTypeAndZone_ZoneName(
            MarkerType markerType,
            String zoneName
    );
}