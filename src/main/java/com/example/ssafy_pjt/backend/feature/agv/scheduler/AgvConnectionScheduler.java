package com.example.ssafy_pjt.backend.feature.agv.scheduler;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AgvConnectionScheduler {

    private static final long TIMEOUT_SECONDS = 10;

    private final AgvRepository agvRepository;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void markTimeoutAgvsOffline() {
        LocalDateTime timeout =
                LocalDateTime.now().minusSeconds(TIMEOUT_SECONDS);

        List<Agv> timedOutAgvs =
                agvRepository.findByLastSeenAtBeforeAndStatusNot(
                        timeout,
                        AgvStatus.OFFLINE
                );

        for (Agv agv : timedOutAgvs) {
            agv.setStatus(AgvStatus.OFFLINE);
            agv.setLastSeenAt(LocalDateTime.now());
        }
    }
}