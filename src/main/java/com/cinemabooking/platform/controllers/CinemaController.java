package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.request.CreateCinemaRequestDTO;
import com.cinemabooking.platform.model.request.UpdateCinemaStatusRequestDTO;
import com.cinemabooking.platform.model.response.CinemaResponseDTO;
import com.cinemabooking.platform.service.CinemaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Cinemas",
        description = "Admin cinema management"
)
@RestController
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @PostMapping
    public ResponseEntity<CinemaResponseDTO> createCinema(
            @Valid @RequestBody CreateCinemaRequestDTO request
    ) {
        CinemaResponseDTO response =
                cinemaService.createCinema(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CinemaResponseDTO>>
    getAllCinemas() {
        return ResponseEntity.ok(
                cinemaService.getAllCinemas()
        );
    }

    @PutMapping("/{cinemaId}/status")
    public ResponseEntity<CinemaResponseDTO> updateCinemaStatus(
            @PathVariable Long cinemaId,
            @Valid
            @RequestBody UpdateCinemaStatusRequestDTO request
    ) {
        CinemaResponseDTO response =
                cinemaService.updateCinemaStatus(
                        cinemaId,
                        request.getActive()
                );

        return ResponseEntity.ok(response);
    }
}