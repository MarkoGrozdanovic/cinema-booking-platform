package com.cinemabooking.platform.scheduler;

import com.cinemabooking.platform.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningCompletionSchedulerTest {

    @Mock
    private ScreeningService screeningService;

    @InjectMocks
    private ScreeningCompletionScheduler scheduler;

    @Test
    void completeEndedScreenings_shouldDelegateToService() {
        when(screeningService.completeEndedScreenings())
                .thenReturn(2);

        scheduler.completeEndedScreenings();

        verify(screeningService).completeEndedScreenings();
    }
}