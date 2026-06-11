package com.example.ssafy_pjt.backend.feature.admin.service;

import com.example.ssafy_pjt.backend.feature.admin.dto.DemoActionRequest;
import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.enums.CargoType;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.event.entity.EventLog;
import com.example.ssafy_pjt.backend.feature.event.enums.EventLevel;
import com.example.ssafy_pjt.backend.feature.event.enums.EventType;
import com.example.ssafy_pjt.backend.feature.event.repository.EventLogRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.enums.InventoryStatus;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.reservation.entity.Reservation;
import com.example.ssafy_pjt.backend.feature.reservation.enums.ReservationStatus;
import com.example.ssafy_pjt.backend.feature.reservation.repository.ReservationRepository;
import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskStatus;
import com.example.ssafy_pjt.backend.feature.task.repository.ProductionTaskRepository;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import com.example.ssafy_pjt.backend.feature.zone.enums.ZoneStatus;
import com.example.ssafy_pjt.backend.feature.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDemoService {

    private final AgvRepository agvRepository;
    private final MissionRepository missionRepository;
    private final ProductionTaskRepository productionTaskRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final ZoneRepository zoneRepository;
    private final EventLogRepository eventLogRepository;

    @Transactional
    public void resetDemoState(DemoActionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        resetMissions(now);
        resetReservations(now);
        resetProductionTasks(now);
        resetAgvs(now);
        resetZones();
        resetInventories(now);

        saveSystemLog(
                EventType.DEMO_RESET,
                EventLevel.WARNING,
                "DEMO STATE RESET by " + safeAdminName(request)
                        + " / reason: " + safeReason(request)
        );
    }

    @Transactional
    public void connectFakeAgv(Integer agvId, DemoActionRequest request) {
        Agv agv = agvRepository.findById(agvId)
                .orElseThrow(() -> new IllegalArgumentException("AGV " + agvId + " not found"));

        agv.setStatus(AgvStatus.IDLE);
        agv.setLastSeenAt(LocalDateTime.now());

        saveSystemLog(
                EventType.AGV_CONNECTED,
                EventLevel.INFO,
                "FAKE AGV" + agvId + " connected by "
                        + safeAdminName(request)
                        + " / reason: " + safeReason(request)
        );
    }

    private void resetMissions(LocalDateTime now) {
        for (Mission mission : missionRepository.findAll()) {
            if (mission.getStatus() == MissionStatus.COMPLETED) {
                continue;
            }

            mission.setStatus(MissionStatus.CANCELLED);
            mission.setCompletedAt(now);
        }
    }

    private void resetReservations(LocalDateTime now) {
        for (Reservation reservation : reservationRepository.findAll()) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservation.setReleasedAt(now);
        }
    }

    private void resetProductionTasks(LocalDateTime now) {
        for (ProductionTask task : productionTaskRepository.findAll()) {
            if (task.getStatus() == TaskStatus.DONE) {
                continue;
            }

            task.setStatus(TaskStatus.CANCELLED);
            task.setCompletedAt(now);
        }
    }

    private void resetAgvs(LocalDateTime now) {
        for (Agv agv : agvRepository.findAll()) {
            agv.setStatus(AgvStatus.OFFLINE);
            agv.setCurrentMission(null);
            agv.setCargoType(CargoType.NONE);
            agv.setCargoMaterial(null);
            agv.setLastSeenAt(now);
        }
    }

    private void resetZones() {
        for (Zone zone : zoneRepository.findAll()) {
            zone.setStatus(ZoneStatus.AVAILABLE);
        }
    }

    private void resetInventories(LocalDateTime now) {
        for (Inventory inventory : inventoryRepository.findAll()) {
            inventory.setReservedQuantity(0);
            inventory.setStatus(InventoryStatus.NORMAL);
            inventory.setUpdatedAt(now);

            inventory.setCurrentQuantity(4);
            inventory.setReservedQuantity(0);
            inventory.setStatus(InventoryStatus.NORMAL);
        }
    }

    private void saveSystemLog(EventType eventType, EventLevel level, String message) {
        EventLog log = new EventLog();

        log.setEventType(eventType);
        log.setLevel(level);
        log.setMessage(message);
        log.setTargetType("SYSTEM");
        log.setTargetId(null);
        log.setCreatedAt(LocalDateTime.now());

        eventLogRepository.save(log);
    }

    private String safeAdminName(DemoActionRequest request) {
        if (request == null || request.adminName() == null || request.adminName().isBlank()) {
            return "UNKNOWN_ADMIN";
        }

        return request.adminName();
    }

    private String safeReason(DemoActionRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            return "no reason";
        }

        return request.reason();
    }
}