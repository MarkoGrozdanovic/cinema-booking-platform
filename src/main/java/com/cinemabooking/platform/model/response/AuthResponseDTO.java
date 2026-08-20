package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.AppRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private AppRole role;
    private String accessToken;
    private String tokenType;
}