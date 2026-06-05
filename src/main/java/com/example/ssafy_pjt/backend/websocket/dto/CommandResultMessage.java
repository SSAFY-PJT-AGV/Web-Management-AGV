package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandResultMessage {

    private String messageType;
    private Integer agvId;

    private String taskId;
    private String commandId;

    private String result; // SUCCESS, FAILED
    private String errorCode;
    private String message;
}