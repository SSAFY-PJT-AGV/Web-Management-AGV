package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandResultMessage {

    private String messageType;

    private Integer agvId;

    private Long taskId;

    private Long commandId;

    private String result;

    private String errorCode;

    private String message;
}