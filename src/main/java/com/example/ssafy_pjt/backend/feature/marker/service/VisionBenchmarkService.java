package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.feature.marker.dto.VisionBenchmarkRequest;
import com.example.ssafy_pjt.backend.feature.marker.dto.VisionBenchmarkResponse;
import com.example.ssafy_pjt.backend.websocket.dto.ArucoResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.ssafy_pjt.backend.feature.marker.dto.VisionStreamingBenchmarkRequest;
import com.example.ssafy_pjt.backend.feature.marker.dto.VisionStreamingBenchmarkResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class VisionBenchmarkService {

    private final ArucoLegacyService legacyService;
    private final ArucoService currentService;

    public VisionBenchmarkResponse benchmark(
            VisionBenchmarkRequest request
    ) {
        int repeat =
                request.repeat() == null || request.repeat() <= 0
                        ? 100
                        : request.repeat();

        Result legacy =
                runLegacy(
                        request.agvId(),
                        request.imageBase64(),
                        repeat
                );

        Result current =
                runCurrent(
                        request.agvId(),
                        request.imageBase64(),
                        repeat
                );

        double improvementRate =
                legacy.avgMs == 0
                        ? 0
                        : ((double) (legacy.avgMs - current.avgMs)
                           / legacy.avgMs) * 100.0;

        return new VisionBenchmarkResponse(
                repeat,

                legacy.avgMs,
                legacy.maxMs,
                legacy.minMs,
                legacy.detectedCount,

                current.avgMs,
                current.maxMs,
                current.minMs,
                current.detectedCount,

                Math.round(improvementRate * 10.0) / 10.0
        );
    }

    private Result runLegacy(
            Integer agvId,
            String imageBase64,
            int repeat
    ) {
        long total = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        int detected = 0;

        for (int i = 0; i < repeat; i++) {
            long start = System.nanoTime();

            ArucoResultMessage result =
                    legacyService.detectFromBase64(
                            agvId,
                            imageBase64
                    );

            long elapsedMs =
                    (System.nanoTime() - start) / 1_000_000;

            total += elapsedMs;
            max = Math.max(max, elapsedMs);
            min = Math.min(min, elapsedMs);

            if (result != null && Boolean.TRUE.equals(result.getDetected())) {
                detected++;
            }
        }

        return new Result(
                total / repeat,
                max,
                min == Long.MAX_VALUE ? 0 : min,
                detected
        );
    }

    private Result runCurrent(
            Integer agvId,
            String imageBase64,
            int repeat
    ) {
        long total = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        int detected = 0;

        for (int i = 0; i < repeat; i++) {
            long start = System.nanoTime();

            ArucoResultMessage result =
                    currentService.benchmarkDetect(
                            agvId,
                            imageBase64
                    );

            long elapsedMs =
                    (System.nanoTime() - start) / 1_000_000;

            total += elapsedMs;
            max = Math.max(max, elapsedMs);
            min = Math.min(min, elapsedMs);

            if (result != null && Boolean.TRUE.equals(result.getDetected())) {
                detected++;
            }
        }

        return new Result(
                total / repeat,
                max,
                min == Long.MAX_VALUE ? 0 : min,
                detected
        );
    }

    private record Result(
            long avgMs,
            long maxMs,
            long minMs,
            int detectedCount
    ) {
    }

    public VisionStreamingBenchmarkResponse benchmarkStreaming(
            VisionStreamingBenchmarkRequest request
    ) {
        int fps =
                request.fps() == null || request.fps() <= 0
                        ? 10
                        : request.fps();

        int durationSec =
                request.durationSec() == null || request.durationSec() <= 0
                        ? 10
                        : request.durationSec();

        int sentFrameCount =
                fps * durationSec;

        LegacyStreamResult legacy =
                runLegacyStreaming(
                        request.agvId(),
                        request.imageBase64(),
                        fps,
                        durationSec,
                        sentFrameCount
                );

        CurrentStreamResult current =
                runCurrentStreaming(
                        request.agvId(),
                        request.imageBase64(),
                        fps,
                        durationSec,
                        sentFrameCount
                );

        return new VisionStreamingBenchmarkResponse(
                fps,
                durationSec,
                sentFrameCount,

                legacy.processedCount,
                legacy.totalElapsedMs,
                legacy.avgProcessMs,
                legacy.maxProcessMs,

                current.processedCount,
                sentFrameCount - current.processedCount,
                current.totalElapsedMs,
                current.avgCallbackDelayMs,
                current.maxCallbackDelayMs
        );
    }

    private LegacyStreamResult runLegacyStreaming(
            Integer agvId,
            String imageBase64,
            int fps,
            int durationSec,
            int sentFrameCount
    ) {
        long intervalMs = 1000L / fps;

        long totalProcessMs = 0;
        long maxProcessMs = 0;
        int processed = 0;

        long testStart = System.currentTimeMillis();

        for (int i = 0; i < sentFrameCount; i++) {
            long frameStart = System.nanoTime();

            legacyService.detectFromBase64(
                    agvId,
                    imageBase64
            );

            long elapsedMs =
                    (System.nanoTime() - frameStart) / 1_000_000;

            totalProcessMs += elapsedMs;
            maxProcessMs = Math.max(maxProcessMs, elapsedMs);
            processed++;

            long targetNextTime =
                    testStart + ((long) (i + 1) * intervalMs);

            long sleepMs =
                    targetNextTime - System.currentTimeMillis();

            if (sleepMs > 0) {
                sleep(sleepMs);
            }
        }

        long totalElapsedMs =
                System.currentTimeMillis() - testStart;

        return new LegacyStreamResult(
                processed,
                totalElapsedMs,
                processed == 0 ? 0 : totalProcessMs / processed,
                maxProcessMs
        );
    }

    private CurrentStreamResult runCurrentStreaming(
            Integer agvId,
            String imageBase64,
            int fps,
            int durationSec,
            int sentFrameCount
    ) {
        long intervalMs = 1000L / fps;

        CountDownLatch latch =
                new CountDownLatch(sentFrameCount);

        AtomicInteger processed =
                new AtomicInteger();

        AtomicLong totalCallbackDelayMs =
                new AtomicLong();

        AtomicLong maxCallbackDelayMs =
                new AtomicLong();

        long testStart =
                System.currentTimeMillis();

        for (int i = 0; i < sentFrameCount; i++) {
            long sentAt =
                    System.currentTimeMillis();

            currentService.submitFrame(
                    agvId,
                    imageBase64,
                    result -> {
                        long delay =
                                System.currentTimeMillis() - sentAt;

                        processed.incrementAndGet();
                        totalCallbackDelayMs.addAndGet(delay);
                        maxCallbackDelayMs.updateAndGet(
                                old -> Math.max(old, delay)
                        );

                        latch.countDown();
                    }
            );

            long targetNextTime =
                    testStart + ((long) (i + 1) * intervalMs);

            long sleepMs =
                    targetNextTime - System.currentTimeMillis();

            if (sleepMs > 0) {
                sleep(sleepMs);
            }
        }

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int processedCount =
                processed.get();

        long totalElapsedMs =
                System.currentTimeMillis() - testStart;

        return new CurrentStreamResult(
                processedCount,
                totalElapsedMs,
                processedCount == 0
                        ? 0
                        : totalCallbackDelayMs.get() / processedCount,
                maxCallbackDelayMs.get()
        );
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record LegacyStreamResult(
            int processedCount,
            long totalElapsedMs,
            long avgProcessMs,
            long maxProcessMs
    ) {
    }

    private record CurrentStreamResult(
            int processedCount,
            long totalElapsedMs,
            long avgCallbackDelayMs,
            long maxCallbackDelayMs
    ) {
    }
}