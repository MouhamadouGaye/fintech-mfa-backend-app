// package com.mgaye.banking_application;

// import static org.assertj.core.api.Assertions.assertThat;

// import java.time.LocalDate;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import com.mgaye.banking_application.dto.ErrorResponse;
// import com.mgaye.banking_application.dto.request.AuthenticationRequest;
// import com.mgaye.banking_application.dto.request.ChangePasswordRequest;
// import com.mgaye.banking_application.dto.request.RegistrationRequest;
// import com.mgaye.banking_application.dto.response.AuthenticationResponse;
// import com.mgaye.banking_application.dto.response.MessageResponse;
// import com.mgaye.banking_application.dto.response.RegistrationResponse;
// import com.mgaye.banking_application.entity.Role;
// import com.mgaye.banking_application.entity.User;
// import com.mgaye.banking_application.repository.UserRepository;

// import jakarta.transaction.Transactional;
// import net.bytebuddy.utility.dispatcher.JavaDispatcher.Container;

// // # ========== INTEGRATION TESTS ==========

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @Testcontainers
// @Transactional
// class BankingSystemIntegrationTest {

// @Container
// static PostgreSQLContainer<?> postgres = new
// PostgreSQLContainer<>("postgres:15-alpine")
// .withDatabaseName("testdb")
// .withUsername("test")
// .withPassword("test");

// @Container
// static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
// .withExposedPorts(6379);

// @Autowired
// private TestRestTemplate restTemplate;

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private PasswordEncoder passwordEncoder;

// @LocalServerPort
// private int port;

// private String baseUrl;

// @BeforeEach
// void setUp() {
// baseUrl = "http://localhost:" + port + "/api";
// }

// @Test
// void fullUserJourney_RegisterLoginChangePassword_ShouldWork() {
// // 1. Register user
// RegistrationRequest registrationRequest = RegistrationRequest.builder()
// .email("integration@test.com")
// .password("TestPass123!")
// .firstName("Integration")
// .lastName("Test")
// .dateOfBirth(LocalDate.of(1990, 1, 1))
// .acceptTerms(true)
// .acceptPrivacy(true)
// .build();

// ResponseEntity<RegistrationResponse> registrationResponse =
// restTemplate.postForEntity(
// baseUrl + "/auth/register",
// registrationRequest,
// RegistrationResponse.class);

// assertThat(registrationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
// assertThat(registrationResponse.getBody().getRequiresEmailVerification()).isTrue();

// // Manually verify user for test
// User user =
// userRepository.findByEmailIgnoreCase("integration@test.com").orElseThrow();
// user.setIsVerified(true);
// userRepository.save(user);

// // 2. Login
// AuthenticationRequest loginRequest = AuthenticationRequest.builder()
// .email("integration@test.com")
// .password("TestPass123!")
// .build();

// ResponseEntity<AuthenticationResponse> loginResponse =
// restTemplate.postForEntity(
// baseUrl + "/auth/login",
// loginRequest,
// AuthenticationResponse.class);

// assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
// assertThat(loginResponse.getBody().getAccessToken()).isNotNull();

// String accessToken = loginResponse.getBody().getAccessToken();

// // 3. Change password
// ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
// .currentPassword("TestPass123!")
// .newPassword("NewTestPass123!")
// .build();

// HttpHeaders headers = new HttpHeaders();
// headers.setBearerAuth(accessToken);
// HttpEntity<ChangePasswordRequest> entity = new
// HttpEntity<>(changePasswordRequest, headers);

// ResponseEntity<MessageResponse> changePasswordResponse =
// restTemplate.postForEntity(
// baseUrl + "/auth/change-password",
// entity,
// MessageResponse.class);

// assertThat(changePasswordResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
// assertThat(changePasswordResponse.getBody().getMessage()).contains("successfully");

// // 4. Login with new password
// AuthenticationRequest newLoginRequest = AuthenticationRequest.builder()
// .email("integration@test.com")
// .password("NewTestPass123!")
// .build();

// ResponseEntity<AuthenticationResponse> newLoginResponse =
// restTemplate.postForEntity(
// baseUrl + "/auth/login",
// newLoginRequest,
// AuthenticationResponse.class);

// assertThat(newLoginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
// assertThat(newLoginResponse.getBody().getAccessToken()).isNotNull();
// }

// @Test
// void authentication_WithMultipleFailedAttempts_ShouldLockAccount() {
// // Create and verify user
// User user = User.builder()
// .email("locktest@test.com")
// .password(passwordEncoder.encode("TestPass123!"))
// .firstName("Lock")
// .lastName("Test")
// .role(Role.CUSTOMER)
// .isActive(true)
// .isVerified(true)
// .build();
// userRepository.save(user);

// AuthenticationRequest wrongPasswordRequest = AuthenticationRequest.builder()
// .email("locktest@test.com")
// .password("WrongPassword")
// .build();

// // Make 5 failed attempts
// for (int i = 0; i < 5; i++) {
// ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
// baseUrl + "/auth/login",
// wrongPasswordRequest,
// ErrorResponse.class);
// assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
// }

// // 6th attempt should return account locked
// ResponseEntity<ErrorResponse> lockedResponse = restTemplate.postForEntity(
// baseUrl + "/auth/login",
// wrongPasswordRequest,
// ErrorResponse.class);

// assertThat(lockedResponse.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
// assertThat(lockedResponse.getBody().getError()).isEqualTo("ACCOUNT_LOCKED");

// // Verify user is locked in database
// User lockedUser =
// userRepository.findByEmailIgnoreCase("locktest@test.com").orElseThrow();
// assertThat(lockedUser.isAccountLocked()).isTrue();
// assertThat(lockedUser.getFailedLoginAttempts()).isEqualTo(5);
// }
// }
