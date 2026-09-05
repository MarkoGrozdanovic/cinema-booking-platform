package com.cinemabooking.platform.scheduler;

import com.cinemabooking.platform.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScreeningCompletionScheduler {

    private final ScreeningService screeningService;

    @Scheduled(fixedDelay = 60_000)
    public void completeEndedScreenings() {
        int completedCount =
                screeningService.completeEndedScreenings();

        if (completedCount > 0) {
            log.info(
                    "Marked {} screenings as completed",
                    completedCount
            );
        }
    }
}