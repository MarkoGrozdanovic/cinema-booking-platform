package com.cinemabooking.platform.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestSecurityExceptionHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        writeResponse(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication is required"
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        writeResponse(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }

    private void writeResponse(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = """
                {
                    "status": %d,
                    "message": "%s",
                    "timestamp": "%s"
                }
                """.formatted(
                status,
                message,
                LocalDateTime.now()
        );

        response.getWriter().write(body);
    }
}