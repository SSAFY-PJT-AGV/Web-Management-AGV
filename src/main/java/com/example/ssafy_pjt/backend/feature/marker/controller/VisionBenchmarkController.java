package com.example.ssafy_pjt.backend.feature.marker.controller;

import com.example.ssafy_pjt.backend.feature.marker.dto.VisionBenchmarkRequest;
import com.example.ssafy_pjt.backend.feature.marker.dto.VisionBenchmarkResponse;
import com.example.ssafy_pjt.backend.feature.marker.service.VisionBenchmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.example.ssafy_pjt.backend.feature.marker.dto.VisionStreamingBenchmarkRequest;
import com.example.ssafy_pjt.backend.feature.marker.dto.VisionStreamingBenchmarkResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/vision")
public class VisionBenchmarkController {

    private final VisionBenchmarkService benchmarkService;

    @PostMapping("/benchmark")
    public VisionBenchmarkResponse benchmark(
            @RequestBody VisionBenchmarkRequest request
    ) {
        return benchmarkService.benchmark(request);
    }

    @PostMapping("/stream-benchmark")
    public VisionStreamingBenchmarkResponse streamBenchmark(
            @RequestBody VisionStreamingBenchmarkRequest request
    ) {
        return benchmarkService.benchmarkStreaming(request);
    }
}