package com.mgaye.banking_application.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgaye.banking_application.dto.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @Component
// public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

//     @Override
//     public void commence(HttpServletRequest request, HttpServletResponse response,
//             AuthenticationException authException) throws IOException, ServletException {

//         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

//         final Map<String, Object> body = new HashMap<>();
//         body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
//         body.put("error", "Unauthorized");
//         body.put("message", authException.getMessage());
//         body.put("path", request.getServletPath());

//         final ObjectMapper mapper = new ObjectMapper();
//         mapper.writeValue(response.getOutputStream(), body);
//     }
// }

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("UNAUTHORIZED")
                .message("Full authentication is required to access this resource")
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
