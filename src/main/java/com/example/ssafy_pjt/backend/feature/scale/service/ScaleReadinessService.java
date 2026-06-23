package com.example.ssafy_pjt.backend.feature.scale.service;


import com.example.ssafy_pjt.backend.feature.scale.ai.GptScaleAnalysisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ScaleReadinessService {

    private final AgvPerformanceMetricService metricService;
    private final GptScaleAnalysisClient aiClient;


    public String analyze(
            int agvCount,
            int fps
    ) {

        var metric =
                metricService.snapshot();


        String prompt = """
        AGV 증설 안정성 분석 요청

        예상 AGV 수:
        %d

        AGV당 FPS:
        %d

        예상 초당 메시지:
        %d msg/s

        =========================
        현재 테스트 결과
        =========================

        총 성공 메시지:
        %d

        전체 실패:
        %d

        =========================
        실패 원인 상세
        =========================

        WebSocket 수신/JSON 실패(receiveFail):
        %d

        Queue 적재 실패(queueReject):
        %d

        Worker 처리 실패(workerFail):
        %d

        AGV 응답 전송 실패(sendFail):
        %d


        =========================
        처리 시간
        =========================

        평균 처리:
        %d ms

        최대 처리:
        %d ms


        위 결과를 기준으로:
        1. 현재 병목 위치
        2. 실시간 AGV 관제 안정성
        3. AGV 증설 가능 여부
        4. 다음 개선 방향

        을 분석해주세요.
        """
                .formatted(
                        agvCount,
                        fps,
                        agvCount * fps,

                        metric.totalMessages(),
                        metric.failedMessages(),

                        metric.receiveFail(),
                        metric.queueReject(),
                        metric.workerFail(),
                        metric.sendFail(),

                        metric.avgProcessMs(),
                        metric.maxProcessMs()
                );


        return aiClient.analyze(prompt);
    }
}