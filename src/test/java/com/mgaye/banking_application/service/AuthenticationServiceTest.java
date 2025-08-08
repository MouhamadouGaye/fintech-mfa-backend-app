package com.mgaye.banking_application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mgaye.banking_application.dto.request.AuthenticationRequest;
import com.mgaye.banking_application.dto.request.RegistrationRequest;
import com.mgaye.banking_application.dto.response.AuthenticationResponse;
import com.mgaye.banking_application.dto.response.RegistrationResponse;
import com.mgaye.banking_application.entity.AmlStatus;
import com.mgaye.banking_application.entity.KycStatus;
import com.mgaye.banking_application.entity.Role;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.AccountSuspendedException;
import com.mgaye.banking_application.repository.UserRepository;
import com.mgaye.banking_application.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SecurityService securityService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private MfaService mfaService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_WithValidCredentials_ShouldReturnToken() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        User user = createTestUser(email);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(email)
                .password(password)
                .build();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("192.168.1.1");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenValidityInSeconds()).thenReturn(900L);

        // When
        AuthenticationResponse response = authenticationService.authenticate(request, httpRequest);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo(email);
        verify(userRepository).save(user);
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowException() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        User user = createTestUser(email);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(email)
                .password(password)
                .build();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(request, httpRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).save(user);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void authenticate_WithSuspendedAccount_ShouldThrowException() {
        // Given
        String email = "test@example.com";
        User user = createTestUser(email);
        user.setIsSuspended(true);
        user.setSuspensionReason("Suspicious activity");

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(email)
                .password("password123")
                .build();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(request, httpRequest))
                .isInstanceOf(AccountSuspendedException.class)
                .hasMessage("Account is suspended: Suspicious activity");
    }

    @Test
    void register_WithValidData_ShouldCreateUser() {
        // Given
        RegistrationRequest request = RegistrationRequest.builder()
                .email("new@example.com")
                .password("StrongPass123!")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .acceptTerms(true)
                .acceptPrivacy(true)
                .build();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("hashed-password");

        // When
        RegistrationResponse response = authenticationService.register(request, httpRequest);

        // Then
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getRequiresEmailVerification()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    private User createTestUser(String email) {
        return User.builder()
                .id(1L)
                .email(email)
                .password("hashed-password")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .isActive(true)
                .isVerified(true)
                .isSuspended(false)
                .isFrozen(false)
                .failedLoginAttempts(0)
                .isMfaEnabled(false)
                .kycStatus(KycStatus.APPROVED)
                .amlStatus(AmlStatus.CLEAR)
                .sessionTimeoutMinutes(30)
                .maxConcurrentSessions(3)
                .build();
    }
}
