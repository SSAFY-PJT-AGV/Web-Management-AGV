package com.example.ssafy_pjt.backend.feature.simulation.dto;

import java.util.List;

public record TempResponse(
        List<AgvItem> agvs
) {
    public record AgvItem(
            Integer agv_id,
            Long task_id,
            String status,
            String from,
            String to,
            Integer current_marker,
            Integer next_marker,
            Position current_position,
            Position next_position,
            String updated_at,
            String payload,
            String debug
    ) {}

    public record Position(
            Double x,
            Double y
    ) {}
}
