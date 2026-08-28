package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.request.RegisterRequestDTO;
import com.cinemabooking.platform.model.response.RegisteredUserResponseDTO;
import com.cinemabooking.platform.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinemabooking.platform.model.request.LoginRequestDTO;
import com.cinemabooking.platform.model.response.AuthResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@Tag(
        name = "Authentication",
        description = "Customer registration and authentication"
)
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request
            ){
        RegisteredUserResponseDTO response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ){
        AuthResponseDTO response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
