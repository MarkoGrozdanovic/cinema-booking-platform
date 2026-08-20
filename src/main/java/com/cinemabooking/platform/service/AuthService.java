package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.LoginRequestDTO;
import com.cinemabooking.platform.model.request.RegisterRequestDTO;
import com.cinemabooking.platform.model.response.AuthResponseDTO;
import com.cinemabooking.platform.model.response.RegisteredUserResponseDTO;

public interface AuthService {
    RegisteredUserResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);
}
