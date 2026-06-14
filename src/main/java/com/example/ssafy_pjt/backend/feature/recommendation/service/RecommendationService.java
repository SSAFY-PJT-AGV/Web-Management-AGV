package com.example.ssafy_pjt.backend.feature.recommendation.service;

import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.material.repository.MaterialRepository;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.recommendation.ai.GmsAiClient;
import com.example.ssafy_pjt.backend.feature.recommendation.dto.AiRecommendationResult;
import com.example.ssafy_pjt.backend.feature.recommendation.dto.RecommendationResponse;
import com.example.ssafy_pjt.backend.feature.recommendation.entity.Recommendation;
import com.example.ssafy_pjt.backend.feature.recommendation.repository.RecommendationRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
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

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations() {
        return recommendationRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(r -> new RecommendationResponse(
                        getTitle(r),
                        r.getReason()
                ))
                .toList();
    }

    private String getTitle(Recommendation r) {

        if (r.getPriorityRank() != null && r.getPriorityRank() == 99) {
            return "AI ANALYSIS";
        }

        if (r.getMaterial() != null) {
            return "INVENTORY WARNING";
        }

        if (r.getReason().contains("Mission Queue")) {
            return "QUEUE BOTTLENECK";
        }

        if (r.getReason().contains("AGV")) {
            return "AGV OPTIMIZATION";
        }

        return "SYSTEM ANALYSIS";
    }

    @Transactional
    public void analyzeRule() {
        int rank = 1;

        rank = analyzeInventory(rank);
        rank = analyzeMissionQueue(rank);
        rank = analyzeAgvLoad(rank);
        analyzeAgvStatus(rank);

        dashboardBroadcastService.aiRefresh();
    }

    @Transactional
    public void analyzeAi() {

        String factoryState =
                factoryStateSummaryService.buildSummary();

        String aiResult =
                gmsAiClient.analyze(factoryState);

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

            if (available <= inventory.getMinThreshold()) {
                Recommendation r = new Recommendation();

                r.setMaterial(inventory.getMaterial());
                r.setPriorityRank(rank++);
                r.setPriorityScore(100.0);
                r.setReason(
                        inventory.getMaterial().getMaterialCode()
                                + " 재고 부족 예상. 자재 보급 Mission 생성을 추천합니다."
                );
                r.setCreatedAt(LocalDateTime.now());

                recommendationRepository.save(r);
            }
        }

        return rank;
    }

    private int analyzeMissionQueue(int rank) {
        long waitingCount = missionRepository.countByStatus(MissionStatus.CREATED);

        if (waitingCount >= 5) {
            Recommendation r = new Recommendation();

            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(80.0);
            r.setReason(
                    "Mission Queue 대기 미션이 "
                            + waitingCount
                            + "개입니다. AGV 작업 병목 가능성이 있습니다."
            );
            r.setCreatedAt(LocalDateTime.now());

            recommendationRepository.save(r);
        }

        return rank;
    }

    private int analyzeAgvLoad(int rank) {
        long agvCount = agvRepository.count();

        if (agvCount <= 0) {
            return rank;
        }

        long waitingCount = missionRepository.countByStatus(MissionStatus.CREATED);

        if (waitingCount >= agvCount * 3) {
            Recommendation r = new Recommendation();

            r.setMaterial(null);
            r.setPriorityRank(rank++);
            r.setPriorityScore(70.0);
            r.setReason(
                    "AGV 대비 대기 Mission 수가 많습니다. 우선순위 기반 스케줄링이 필요합니다."
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
        r.setCreatedAt(LocalDateTime.now());

        recommendationRepository.save(r);
    }

    private Material resolveMaterialOrNull(String targetKey) {
        if (targetKey == null || targetKey.isBlank()) {
            return null;
        }

        return switch (targetKey.trim()) {
            case "CHIP", "SENSOR", "BATTERY" ->
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