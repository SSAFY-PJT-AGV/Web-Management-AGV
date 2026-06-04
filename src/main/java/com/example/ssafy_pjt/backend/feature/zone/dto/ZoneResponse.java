package com.example.ssafy_pjt.backend.feature.zone.dto;

import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import lombok.Getter;

@Getter
public class ZoneResponse {
    private final Long zoneId;
    private final String zoneName;
    private final String zoneType;
    private final String status;

    public ZoneResponse(Zone zone) {
        this.zoneId = zone.getZoneId();
        this.zoneName = zone.getZoneName();
        this.zoneType = zone.getZoneType().name();
        this.status = zone.getStatus().name();
    }
}
