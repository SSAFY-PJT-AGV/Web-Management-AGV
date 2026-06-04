package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgvStatusMessage {

    private Integer agvId;
    private Long timestamp;

    private String status;
    private String event;

    private String taskId;
    private String commandId;

    private Integer located;
    private Integer destination;

    private String cargo;

    private Float speed;
    private Boolean isCW;

    private Boolean hasImage;
    private String image;
}