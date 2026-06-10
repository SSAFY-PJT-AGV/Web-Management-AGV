package com.example.ssafy_pjt.backend.websocket.sender;

import com.example.ssafy_pjt.backend.websocket.event.DashboardEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardBroadcastService {

    private final DashboardSender dashboardSender;

    public void taskRefresh() {
        broadcastRefreshAfterCommit(DashboardEventType.TASK_REFRESH);
    }

    public void missionRefresh() {
        broadcastRefreshAfterCommit(DashboardEventType.MISSION_REFRESH);
    }

    public void inventoryRefresh() {
        broadcastRefreshAfterCommit(DashboardEventType.INVENTORY_REFRESH);
    }

    public void eventRefresh() {
        broadcastRefreshAfterCommit(DashboardEventType.EVENT_REFRESH);
    }

    public void aiRefresh() {
        broadcastRefreshAfterCommit(DashboardEventType.AI_REFRESH);
    }

    public void mapRefresh() {
        broadcastRefreshAfterCommit(DashboardEventType.MAP_REFRESH);
    }

    public void agvStatus(Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", DashboardEventType.AGV_STATUS);
        payload.put("data", data);

        broadcastAfterCommit(payload);
    }

    private void broadcastRefreshAfterCommit(String type) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);

        broadcastAfterCommit(payload);
    }

    private void broadcastAfterCommit(Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            dashboardSender.broadcast(payload);
                        }
                    }
            );
        } else {
            dashboardSender.broadcast(payload);
        }
    }
}