package com.example.ssafy_pjt.backend.feature.scale.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class AgvPerformanceMetricService {

    private final AtomicLong totalMessages = new AtomicLong();

    // 실패 상세 분류
    private final AtomicLong receiveFailMessages = new AtomicLong();
    private final AtomicLong queueRejectMessages = new AtomicLong();
    private final AtomicLong workerFailMessages = new AtomicLong();
    private final AtomicLong sendFailMessages = new AtomicLong();

    private final AtomicLong totalProcessTimeMs = new AtomicLong();
    private final AtomicLong maxProcessTimeMs = new AtomicLong();

    public long start() {
        return System.currentTimeMillis();
    }

    public void success(long startTime) {

        long elapsed =
                System.currentTimeMillis() - startTime;

        totalMessages.incrementAndGet();

        totalProcessTimeMs.addAndGet(elapsed);

        maxProcessTimeMs.updateAndGet(
                old -> Math.max(old, elapsed)
        );
    }


    /*
     * WebSocket JSON 파싱 실패
     * 잘못된 Payload
     */
    public void receiveFail() {
        receiveFailMessages.incrementAndGet();
    }


    /*
     * Queue 용량 초과
     * 서버 처리량 부족
     */
    public void queueReject() {
        queueRejectMessages.incrementAndGet();
    }


    /*
     * Worker 내부 처리 실패
     * DB, Scheduler, Business Logic 예외
     */
    public void workerFail() {
        workerFailMessages.incrementAndGet();
    }


    /*
     * AGV 응답 전송 실패
     */
    public void sendFail() {
        sendFailMessages.incrementAndGet();
    }


    public MetricSnapshot snapshot() {

        long total = totalMessages.get();

        long totalFail =
                receiveFailMessages.get()
                        + queueRejectMessages.get()
                        + workerFailMessages.get()
                        + sendFailMessages.get();

        return new MetricSnapshot(
                total,
                totalFail,

                receiveFailMessages.get(),
                queueRejectMessages.get(),
                workerFailMessages.get(),
                sendFailMessages.get(),

                total == 0
                        ? 0
                        : totalProcessTimeMs.get() / total,

                maxProcessTimeMs.get()
        );
    }


    public record MetricSnapshot(
            long totalMessages,

            long failedMessages,

            long receiveFail,
            long queueReject,
            long workerFail,
            long sendFail,

            long avgProcessMs,
            long maxProcessMs
    ) {
    }
}