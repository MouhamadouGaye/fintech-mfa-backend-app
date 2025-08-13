package com.mgaye.banking_application.controller;

import com.mgaye.banking_application.Controller.AuthController;
import com.mgaye.banking_application.dto.LoginDto;
import com.mgaye.banking_application.dto.UserRegistrationDto;
import com.mgaye.banking_application.security.JwtHelper;
import com.mgaye.banking_application.service.UserDetailsServiceImpl;
import com.mgaye.banking_application.service.UserService;
import com.mgaye.banking_application.service.AuditService;
import com.mgaye.banking_application.service.AuthenticationService;
import com.mgaye.banking_application.service.MfaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private MfaService mfaService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtHelper jwtHelper;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginDto loginDto;
    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        loginDto = new LoginDto();
        loginDto.setEmail("testuser");
        loginDto.setPassword("password");

        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("Password@123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
    }

    @Test
    public void testVerifyEmail() throws Exception {
        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", "someVerificationToken"))
                .andExpect(status().isOk())
                .andExpect(view().name("email-verification-success"));
    }

}
