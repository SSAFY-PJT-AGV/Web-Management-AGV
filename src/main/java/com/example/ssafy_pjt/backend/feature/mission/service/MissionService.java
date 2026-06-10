package com.example.ssafy_pjt.backend.feature.mission.service;

import com.example.ssafy_pjt.backend.feature.mission.dto.MissionResponse;
import com.example.ssafy_pjt.backend.feature.mission.dto.MissionSummaryResponse;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public List<MissionResponse> getMissions() {
        return missionRepository.findAll()
                .stream()
                .map(MissionResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MissionSummaryResponse> getMissionSummary() {
        return missionRepository.findByStatusNotOrderBySequenceOrderAsc(MissionStatus.COMPLETED)
                .stream()
                .map(mission -> new MissionSummaryResponse(
                        mission.getSequenceOrder(),
                        toJobName(mission.getMissionType()),
                        mission.getAgv() == null
                                ? "배정 대기"
                                : "AGV%02d".formatted(mission.getAgv().getAgvId()),
                        toDisplayStatus(mission.getStatus())
                ))
                .toList();
    }

    private String toJobName(MissionType missionType) {
        return switch (missionType) {
            case PICK_FROM_STORAGE -> "자재 공급 작업";
            case DROP_TO_CONVEYOR -> "컨베이어 이동 작업";
            case PICK_EMPTY_BOX -> "빈 박스 회수 작업";
            case DROP_EMPTY_BOX -> "빈 박스 보관 작업";
            case PICK_FROM_CONVEYOR -> "완제품 회수 작업";
            case DROP_TO_FINISHED_BOX_STORAGE -> "완제품 보관 작업";
            case PICK_FROM_INBOUND -> "입고 구역 이동 작업";
            case DROP_TO_OUTBOUND -> "출고 구역 이동 작업";
            case PICK_FROM_CROSS -> "교차 구역 회수 작업";
            case DROP_TO_CROSS -> "교차 구역 전달 작업";
            case RETURN_TO_BASE -> "복귀 작업";
            case WAIT -> "대기 작업";
            case STOP -> "정지 명령";
            case RESUME -> "재개 명령";
            default -> "AGV 작업";
        };
    }

    private String toDisplayStatus(MissionStatus status) {
        return switch (status) {
            case CREATED -> "대기중";
            case ASSIGNED -> "배정됨";
            case IN_PROGRESS -> "진행중";
            case COMPLETED -> "완료";
            case FAILED -> "실패";
            case CANCELLED -> "취소";
        };
    }
}
