package com.example.ssafy_pjt.backend.feature.mission.enums;

public enum MissionType {
    // =====================
    // AGV01 기본 자재 공급
    // =====================
    PICK_FROM_STORAGE,
    DROP_TO_CONVEYOR,

    // =====================
    // AGV01 보급 처리
    // =====================
    PICK_FROM_CROSS,
    DROP_TO_STORAGE,

    // =====================
    // AGV02 완제품 회수
    // =====================
    PICK_FROM_CONVEYOR,
    DROP_TO_FINISHED_BOX_STORAGE,
    PICK_FROM_FINISHED_BOX_STORAGE,

    // =====================
    // AGV02 입출고 작업
    // =====================
    PICK_FROM_INBOUND,
    DROP_TO_OUTBOUND,

    // =====================
    // 교차구역 전달
    // =====================
    DROP_TO_CROSS,
    PICK_EMPTY_BOX,
    DROP_EMPTY_BOX,

    // =====================
    // 공통 제어
    // =====================
    RETURN_TO_BASE,
    WAIT,
    STOP,
    RESUME
}
