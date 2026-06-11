package com.example.ssafy_pjt.backend.feature.simulation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationAgvResponse(
        List<SimulationAgvItem> agvs
) {
    public record SimulationAgvItem(
            @JsonProperty("agv_id")
            Integer agvId,

            @JsonProperty("task_id")
            Long taskId,

            String status,

            String from,
            String to,

            @JsonProperty("current_marker")
            Integer currentMarker,

            @JsonProperty("next_marker")
            Integer nextMarker,

            @JsonProperty("current_position")
            Position currentPosition,

            @JsonProperty("next_position")
            Position nextPosition,

            @JsonProperty("updated_at")
            LocalDateTime updatedAt,

            String payload,
            String debug
    ) {}

    public record Position(
            Double x,
            Double y
    ) {}
}