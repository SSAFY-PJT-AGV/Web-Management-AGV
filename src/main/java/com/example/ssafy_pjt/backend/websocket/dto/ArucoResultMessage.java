package com.example.ssafy_pjt.backend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArucoResultMessage {

    private String messageType;
    private String type;
    private Integer agvId;
    private Boolean detected;

    @JsonProperty("marker_count")
    private Integer markerCount;

    @JsonProperty("image_width")
    private Integer imageWidth;

    @JsonProperty("image_height")
    private Integer imageHeight;

    private List<MarkerInfo> markers;
    private Double timestamp;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarkerInfo {

        @JsonProperty("marker_id")
        private Integer markerId;

        private Corners corners;
        private List<Double> center;

        @JsonProperty("x_error")
        private Double xError;

        @JsonProperty("y_error")
        private Double yError;

        private List<Double> rvec;
        private List<Double> tvec;

        private Double tx;
        private Double ty;
        private Double tz;

        private Double distance;
        private Double yaw;
        private Double pitch;

        private Double width;
        private Double height;
        private Double area;

        @JsonProperty("x_centered")
        private Boolean xCentered;

        @JsonProperty("y_centered")
        private Boolean yCentered;

        private Boolean centered;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Corners {

        @JsonProperty("top_left")
        private List<Double> topLeft;

        @JsonProperty("top_right")
        private List<Double> topRight;

        @JsonProperty("bottom_right")
        private List<Double> bottomRight;

        @JsonProperty("bottom_left")
        private List<Double> bottomLeft;
    }
}