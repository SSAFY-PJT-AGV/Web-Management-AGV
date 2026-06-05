package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisionResultMessage {

    private String messageType;
    private Integer agvId;
    private Integer markerId;
    private Double distance;
    private Double angle;
}