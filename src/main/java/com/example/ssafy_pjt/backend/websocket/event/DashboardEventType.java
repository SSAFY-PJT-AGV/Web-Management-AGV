package com.example.ssafy_pjt.backend.websocket.event;

public final class DashboardEventType {

    private DashboardEventType() {
    }

    public static final String AGV_STATUS = "AGV_STATUS";

    public static final String TASK_REFRESH = "TASK_REFRESH";
    public static final String MISSION_REFRESH = "MISSION_REFRESH";
    public static final String INVENTORY_REFRESH = "INVENTORY_REFRESH";
    public static final String EVENT_REFRESH = "EVENT_REFRESH";
    public static final String AI_REFRESH = "AI_REFRESH";
    public static final String MAP_REFRESH = "MAP_REFRESH";
}