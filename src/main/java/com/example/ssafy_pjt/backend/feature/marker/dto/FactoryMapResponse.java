package com.example.ssafy_pjt.backend.feature.marker.dto;

import java.util.List;

public record FactoryMapResponse(
        List<MarkerMapItem> markers,
        List<AgvMapItem> agvs
) {}