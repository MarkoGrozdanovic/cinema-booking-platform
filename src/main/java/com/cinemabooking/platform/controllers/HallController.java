package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.request.CreateHallRequestDTO;
import com.cinemabooking.platform.model.response.HallResponseDTO;
import com.cinemabooking.platform.service.HallService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinemabooking.platform.model.request.UpdateHallStatusRequestDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;

@Tag(
        name = "Cinema halls",
        description = "Admin cinema hall and seat-layout management"
)
@RestController
@RequestMapping("/api/admin/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PostMapping
    public ResponseEntity<HallResponseDTO> createHall(
            @Valid @RequestBody CreateHallRequestDTO request
    ) {
        HallResponseDTO response =
                hallService.createHall(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<HallResponseDTO>>
    getAllHalls() {
        return ResponseEntity.ok(
                hallService.getAllHalls()
        );
    }

    @PutMapping("/{hallId}/status")
    public ResponseEntity<HallResponseDTO> updateHallStatus(
            @PathVariable Long hallId,
            @Valid
            @RequestBody UpdateHallStatusRequestDTO request
    ) {
        HallResponseDTO response =
                hallService.updateHallStatus(
                        hallId,
                        request.getActive()
                );

        return ResponseEntity.ok(response);
    }
}