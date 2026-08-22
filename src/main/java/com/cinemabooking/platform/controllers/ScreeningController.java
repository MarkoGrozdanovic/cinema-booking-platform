package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.model.response.ScreeningSeatResponseDTO;
import com.cinemabooking.platform.service.ScreeningSerivce;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScreeningController {

    private final ScreeningSerivce screeningService;

    public ScreeningController(ScreeningSerivce screeningSerivce) {
        this.screeningService = screeningSerivce;
    }

    @PostMapping("/screenings")
    public ResponseEntity<ScreeningResponseDTO> createScreening(@Valid @RequestBody CreateScreeningRequestDTO request) {
        return new ResponseEntity<>(screeningService.createScreening(request), HttpStatus.CREATED);
    }

    @GetMapping("/screenings/{screeningId}/seats")
    public ResponseEntity<List<ScreeningSeatResponseDTO>>
    getScreeningSeats(
            @PathVariable Long screeningId
    ) {
        List<ScreeningSeatResponseDTO> seats =
                screeningService.getScreeningSeats(screeningId);

        return ResponseEntity.ok(seats);
    }
}
