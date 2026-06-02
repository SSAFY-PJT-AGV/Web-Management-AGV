package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VisionResultMessage {

    private String messageType;
    private String agvId;

    private Integer markerId;
    private Double distance;
    private Double angle;
}