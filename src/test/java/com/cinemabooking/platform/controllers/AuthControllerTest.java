package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.request.RegisterRequestDTO;
import com.cinemabooking.platform.model.response.RegisteredUserResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.cinemabooking.platform.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import com.cinemabooking.platform.model.request.LoginRequestDTO;
import com.cinemabooking.platform.model.response.AuthResponseDTO;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRegisterCustomer() throws Exception {
        RegisteredUserResponseDTO response =
                RegisteredUserResponseDTO.builder()
                        .id(1L)
                        .firstName("Marko")
                        .lastName("Grozdanovic")
                        .email("marko@example.com")
                        .role(AppRole.CUSTOMER)
                        .build();

        when(authService.register(
                any(RegisterRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
                {
                    "firstName": "Marko",
                    "lastName": "Grozdanovic",
                    "email": "marko@example.com",
                    "password": "SecurePassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName")
                        .value("Grozdanovic"))
                .andExpect(jsonPath("$.email")
                        .value("marko@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .userId(1L)
                .firstName("Marko")
                .lastName("Grozdanovic")
                .email("marko@example.com")
                .role(AppRole.CUSTOMER)
                .accessToken("generated-jwt-token")
                .tokenType("Bearer")
                .build();

        when(authService.login(
                any(LoginRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
            {
                "email": "marko@example.com",
                "password": "SecurePassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email")
                        .value("marko@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"))
                .andExpect(jsonPath("$.accessToken")
                        .value("generated-jwt-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRegistration()
            throws Exception {

        String requestBody = """
            {
                "firstName": "",
                "lastName": "",
                "email": "invalid-email",
                "password": "short"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never())
                .register(any(RegisterRequestDTO.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidLogin()
            throws Exception {

        String requestBody = """
            {
                "email": "invalid-email",
                "password": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never())
                .login(any(LoginRequestDTO.class));
    }
}