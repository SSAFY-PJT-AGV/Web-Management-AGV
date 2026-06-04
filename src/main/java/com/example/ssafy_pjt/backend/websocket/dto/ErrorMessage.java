package com.example.ssafy_pjt.backend.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorMessage {

    private String messageType;
    private Integer agvId;
    private String message;
}