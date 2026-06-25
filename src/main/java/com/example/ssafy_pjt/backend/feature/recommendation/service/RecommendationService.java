package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.material.repository.MaterialRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.mission.service.ReplenishmentService;
import com.example.ssafy_pjt.backend.feature.recommendation.ai.GmsAiClient;
import com.example.ssafy_pjt.backend.feature.recommendation.dto.AiRecommendationResult;
import com.example.ssafy_pjt.backend.feature.recommendation.dto.RecommendationResponse;
import com.example.ssafy_pjt.backend.feature.recommendation.entity.Recommendation;
import com.example.ssafy_pjt.backend.feature.recommendation.repository.RecommendationRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import com.example.ssafy_pjt.backend.feature.event.repository.EventLogRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionRepository missionRepository;
    private final AgvRepository agvRepository;
    private final DashboardBroadcastService dashboardBroadcastService;
    private final FactoryStateSummaryService factoryStateSummaryService;
    private final MaterialRepository materialRepository;
    private final GmsAiClient gmsAiClient;
    private final ReplenishmentService replenishmentService;
    private final EventLogRepository eventLogRepository;

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations() {
        return recommendationRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(r -> new RecommendationResponse(
                        getTitle(r),
                        r.getReason(),
                        r.getPriorityScore(),
                        r.getTargetKey()
                ))
                .toList();
    }

    private String getTitle(Recommendation r) {
        if (r.getPriorityRank() != null && r.getPriorityRank() == 99) {
            return "LLM ANALYSIS";
        }

        if (r.getMaterial() != null) {
            return "RULE INVENTORY WARNING";
        }

        if (r.getReason().contains("대기 Mission")) {
            return "RULE BOTTLENECK";
        }

        if (r.getReason().contains("이벤트 로그")) {
            return "RULE EVENT WARNING";
        }

        if (r.getReason().contains("AGV")) {
            return "RULE AGV OPTIMIZATION";
        }

        return "RULE SYSTEM ANALYSIS";
    }

    @Transactional
    public void analyzeRule() {
        recommendationRepository.deleteAll();

        int rank = 1;

        rank = analyzeInventory(rank);
        rank = analyzeMissionWaitingTime(rank);
        rank = analyzeEventLog(rank);
        rank = analyzeAgvLoad(rank);
        analyzeAgvStatus(rank);

        dashboardBroadcastService.aiRefresh();
    }

    @Transactional
    public void analyzeAi() {
        String factoryState = factoryStateSummaryService.buildSummary();

        String aiResult = gmsAiClient.analyze(factoryState);

        System.out.println("[AI RESULT]");
        System.out.println(aiResult);

        saveAiRecommendation(aiResult);

        dashboardBroadcastService.aiRefresh();
    }

    private int analyzeAgvStatus(int rank) {
        var agvs = agvRepository.findAll();

        for (var agv : agvs) {
            if (agv.getStatus().name().equals("OFFLINE")
                    && agv.getCurrentMission() != null) {

                Recommendation r = new Recommendation();

                r.setMaterial(null);
                r.setPriorityRank(rank++);
                r.setPriorityScore(90.0);
                r.setReason(
                        "AGV"
                                + String.format("%02d", agv.getAgvId())
                                + " 연결이 끊겼지만 Mission #"
                                + agv.getCurrentMission().getMissionId()
                                + " 이 남아있습니다. 상태 확인이 필요합니다."
                );
                r.setTargetKey("AGV" + String.format("%02d", agv.getAgvId()));
                r.setCreatedAt(LocalDateTime.now());

                recommendationRepository.save(r);
            }
        }

        return rank;
    }

    private int analyzeInventory(int rank) {
        List<Inventory> inventories = inventoryRepository.findAll();

        for (Inventory inventory : inventories) {
            int available = inventory.getCurrentQuantity() - inventory.getReservedQuantity();

            if (available <= 0) {
                Recommendation r = new Recommendation();

                r.setMaterial(inventory.getMaterial());
                r.setPriorityRank(rank++);
                r.setPriorityScore(100.0);
                r.setReason(
                        inventory.getMaterial().getMaterialCode()
                                + " 재고가 0입니다. 자재 보급 Mission 생성이 필요합니다."
                );
                r.setTargetKey(inventory.getMaterial().getMaterialCode());
                r.setCreatedAt(LocalDateTime.now());

                recommendationRepository.save(r);

                createReplenishmentIfShortage(inventory.getMaterial().getMaterialCode());
            }
        }

        return rank;
    }

    private int analyzeMissionWaitingTime(int rank) {
        List<Mission> missions = missionRepository.findAll();

        LocalDateTime now = LocalDateTime.now();

        long agv1MaxWait = 0;
        long agv2MaxWait = 0;

        for (Mission mission : missions) {
            if (!List.of(
                    MissionStatus.CREATED,
                    MissionStatus.ASSIGNED
            ).contains(mission.getStatus())) {
                continue;
            }

            if (mission.getAgv() == null || mission.getCreatedAt() == null) {
                continue;
            }

            long waitSec = Math.max(
                    0,
                    Duration.between(mission.getCreatedAt(), now).toSeconds()
            );

            Integer agvId = mission.getAgv().getAgvId();

            if (agvId == 1) {
                agv1MaxWait = Math.max(agv1MaxWait, waitSec);
            } else if (agvId == 2) {
                agv2MaxWait = Math.max(agv2MaxWait, waitSec);
            }
        }

        long thresholdSec = 20;
        long diffThresholdSec = 10;

        if (agv1MaxWait >= thresholdSec
                && agv1MaxWait - agv2MaxWait >= diffThresholdSec) {

            Recommendation r = new Recommendation();
            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(82.0);
            r.setReason(
                    "[RULE] AGV01 대기 Mission의 최대 대기 시간이 "
                            + agv1MaxWait
                            + "초입니다. AGV02보다 대기 시간이 길어 SUPPLY 구간 병목 가능성이 있습니다."
            );
            r.setTargetKey("AGV01_WAITING_BOTTLENECK");
            r.setCreatedAt(LocalDateTime.now());

            recommendationRepository.save(r);
        }

        if (agv2MaxWait >= thresholdSec
                && agv2MaxWait - agv1MaxWait >= diffThresholdSec) {

            Recommendation r = new Recommendation();
            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(82.0);
            r.setReason(
                    "[RULE] AGV02 대기 Mission의 최대 대기 시간이 "
                            + agv2MaxWait
                            + "초입니다. AGV01보다 대기 시간이 길어 COLLECT 구간 병목 가능성이 있습니다."
            );
            r.setTargetKey("AGV02_WAITING_BOTTLENECK");
            r.setCreatedAt(LocalDateTime.now());

            recommendationRepository.save(r);
        }

        if (agv1MaxWait >= thresholdSec && agv2MaxWait >= thresholdSec) {
            Recommendation r = new Recommendation();
            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(78.0);
            r.setReason(
                    "[RULE] AGV01/AGV02 모두 대기 Mission이 "
                            + thresholdSec
                            + "초 이상 누적되어 전체 처리 지연 가능성이 있습니다."
            );
            r.setTargetKey("MISSION_WAITING_DELAY");
            r.setCreatedAt(LocalDateTime.now());

            recommendationRepository.save(r);
        }

        return rank;
    }

    private int analyzeEventLog(int rank) {
        var events = eventLogRepository.findTop50ByOrderByCreatedAtDesc();

        long warningOrErrorCount = events.stream()
                .limit(20)
                .filter(event ->
                        "WARNING".equals(String.valueOf(event.getLevel()))
                                || "ERROR".equals(String.valueOf(event.getLevel()))
                )
                .count();

        if (warningOrErrorCount >= 3) {
            Recommendation r = new Recommendation();

            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(88.0);
            r.setReason(
                    "[RULE] 최근 이벤트 로그 20건 중 WARNING/ERROR가 "
                            + warningOrErrorCount
                            + "건 발생했습니다. 반복 오류 또는 비정상 상태 흐름 확인이 필요합니다."
            );
            r.setTargetKey("EVENT_LOG_WARNING");
            r.setCreatedAt(LocalDateTime.now());

            recommendationRepository.save(r);
        }

        return rank;
    }

    private int analyzeAgvLoad(int rank) {
        long failedCount =
                missionRepository.countByStatus(MissionStatus.FAILED);

        if (failedCount > 0) {
            Recommendation r = new Recommendation();

            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(85.0);
            r.setReason(
                    "FAILED Mission이 발생했습니다. AGV 상태와 Mission 처리 흐름 확인이 필요합니다."
            );
            r.setCreatedAt(LocalDateTime.now());

            recommendationRepository.save(r);
        }

        return rank;
    }

    private void saveAiRecommendation(String aiResult) {
        AiRecommendationResult parsed = parseAiResult(aiResult);

        Recommendation r = new Recommendation();

        r.setMaterial(resolveMaterialOrNull(parsed.targetKey()));
        r.setPriorityRank(99);
        r.setPriorityScore(parsed.priorityScore());
        r.setReason(parsed.message());
        r.setTargetKey(parsed.targetKey());
        r.setCreatedAt(LocalDateTime.now());

        recommendationRepository.save(r);

        createReplenishmentIfShortage(parsed.targetKey());
    }

    private void createReplenishmentIfShortage(String targetKey) {
        Material material = resolveMaterialOrNull(targetKey);

        if (material == null) {
            return;
        }

        Inventory inventory = inventoryRepository.findAll()
                .stream()
                .filter(i -> i.getMaterial() != null)
                .filter(i -> i.getMaterial().getMaterialId().equals(material.getMaterialId()))
                .findFirst()
                .orElse(null);

        if (inventory == null) {
            return;
        }

        int available = inventory.getCurrentQuantity() - inventory.getReservedQuantity();

        if (available > 0) {
            return;
        }

        if (hasActiveReplenishmentMission(material)) {
            System.out.println("[AI REPLENISHMENT SKIP] active replenishment mission exists. material="
                    + material.getMaterialCode());
            return;
        }

        replenishmentService.createReplenishmentMissions(
                material.getMaterialCode(),
                inventory.getMinThreshold()
        );

        System.out.println("[AI REPLENISHMENT CREATED] material="
                + material.getMaterialCode());
    }

    private boolean hasActiveReplenishmentMission(Material material) {
        return missionRepository.findAll()
                .stream()
                .filter(mission -> mission.getMaterial() != null)
                .filter(mission -> mission.getMaterial().getMaterialId().equals(material.getMaterialId()))
                .filter(mission -> List.of(
                        MissionStatus.CREATED,
                        MissionStatus.ASSIGNED,
                        MissionStatus.IN_PROGRESS
                ).contains(mission.getStatus()))
                .anyMatch(this::isReplenishmentMission);
    }

    private boolean isReplenishmentMission(Mission mission) {
        return List.of(
                MissionType.PICK_FROM_INBOUND,
                MissionType.DROP_TO_CROSS,
                MissionType.PICK_FROM_CROSS,
                MissionType.DROP_TO_STORAGE
        ).contains(mission.getMissionType());
    }

    private Material resolveMaterialOrNull(String targetKey) {
        if (targetKey == null || targetKey.isBlank()) {
            return null;
        }

        return switch (targetKey.trim()) {
            case "CHIP", "SENSOR" ->
                    materialRepository.findByMaterialCode(targetKey.trim())
                            .orElse(null);

            default -> null;
        };
    }

    private double parseScore(String aiResult) {
        try {
            String marker = "우선도점수:";
            int start = aiResult.indexOf(marker);

            if (start == -1) {
                return 50.0;
            }

            String scoreText = aiResult
                    .substring(start + marker.length())
                    .trim()
                    .split("\\s+")[0];

            return Double.parseDouble(scoreText);

        } catch (Exception e) {
            return 50.0;
        }
    }

    private AiRecommendationResult parseAiResult(String aiResult) {
        return new AiRecommendationResult(
                extractSection(aiResult, "제목:", "내용:"),
                extractSection(aiResult, "내용:", "우선도점수:"),
                parseScore(aiResult),
                extractSection(aiResult, "추천대상:", "추천키:"),
                extractSection(aiResult, "추천키:", null)
        );
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);

        if (start == -1) {
            return "";
        }

        start += startMarker.length();

        int end = endMarker == null
                ? text.length()
                : text.indexOf(endMarker, start);

        if (end == -1) {
            end = text.length();
        }

        return text.substring(start, end).trim();
    }

    public void updateRecommendation() {
        analyzeRule();
    }
}