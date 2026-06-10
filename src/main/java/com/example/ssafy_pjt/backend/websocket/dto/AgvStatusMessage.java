package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgvStatusMessage {

    private String messageType;

    private Integer agvId;
    private Long timestamp;

    private String status;
    private String event;

    private Long taskId;
    private Long commandId;

    private Integer located;
    private Integer destination;

    private String cargo;
    private Boolean isCW;

    private Boolean hasImage;
    private String image;
}