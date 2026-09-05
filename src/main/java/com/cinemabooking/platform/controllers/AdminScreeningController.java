package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/screenings")
@RequiredArgsConstructor
public class AdminScreeningController {

    private final ScreeningService screeningService;

    @GetMapping
    public ResponseEntity<List<ScreeningResponseDTO>> getAllScreenings() {
        return ResponseEntity.ok(
                screeningService.getAllScreenings()
        );
    }

    @PatchMapping("/{screeningId}/cancel")
    public ResponseEntity<ScreeningResponseDTO> cancelScreening(
            @PathVariable Long screeningId
    ) {
        return ResponseEntity.ok(
                screeningService.cancelScreening(screeningId)
        );
    }
}