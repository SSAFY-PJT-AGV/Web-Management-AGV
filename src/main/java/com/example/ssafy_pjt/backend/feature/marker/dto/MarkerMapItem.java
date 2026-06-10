package com.example.ssafy_pjt.backend.feature.marker.dto;

public record MarkerMapItem(
        Integer markerId,
        Double x,
        Double y,
        String zoneName,
        String type
) {
}