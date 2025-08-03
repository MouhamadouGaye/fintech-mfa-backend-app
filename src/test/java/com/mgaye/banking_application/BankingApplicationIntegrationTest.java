// package com.mgaye.banking_application;

// import com.mgaye.banking_application.dto.LoginDto;
// import com.mgaye.banking_application.dto.UserRegistrationDto;
// import com.mgaye.banking_application.dto.UserResponseDto;
// import com.mgaye.banking_application.entity.User;
// import com.mgaye.banking_application.exception.UserAlreadyExistsException;
// import com.mgaye.banking_application.exception.UserNotFoundException;
// import com.mgaye.banking_application.repository.UserRepository;
// import com.mgaye.banking_application.service.UserService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.context.WebApplicationContext;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.eq;
// import static org.mockito.Mockito.anyString;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import java.util.Optional;

// @SpringBootTest
// @AutoConfigureWebMvc
// @ActiveProfiles("test")
// @Transactional
// class BankingApplicationIntegrationTest {

// @Autowired
// private WebApplicationContext webApplicationContext;

// @Autowired
// private PasswordEncoder passwordEncoder;;

// @Autowired
// private UserRepository userRepository;

// private UserService userService;

// @Autowired
// private ObjectMapper objectMapper;

// private MockMvc mockMvc;

// // Add mock for auditService
// @org.mockito.Mock
// private com.mgaye.banking_application.service.AuditService auditService;

// @org.mockito.Mock
// private User user;

// @org.mockito.Mock
// private UserRegistrationDto registrationDto;

// @org.junit.jupiter.api.BeforeEach
// void initMocks() {
// org.mockito.MockitoAnnotations.openMocks(this);
// userService = new UserService(userRepository, passwordEncoder, auditService);
// }

// @BeforeEach
// void setUp() {
// mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
// userRepository.deleteAll();
// }

// @Test
// void fullUserRegistrationAndLoginFlow() throws Exception {
// // Step 1: Register a new user
// UserRegistrationDto registrationDto = new UserRegistrationDto();
// registrationDto.setUsername("integrationtest");
// registrationDto.setEmail("integration@test.com");
// registrationDto.setPassword("Integration@123");
// registrationDto.setFirstName("Integration");
// registrationDto.setLastName("Test");
// registrationDto.setPhoneNumber("+1234567890");

// mockMvc.perform(post("/api/auth/register")
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(registrationDto)))
// .andExpect(status().isCreated())
// .andExpect(jsonPath("$.success").value(true))
// .andExpect(jsonPath("$.data.username").value("integrationtest"));

// // Verify user is saved in database
// User savedUser =
// userRepository.findByUsername("integrationtest").orElse(null);
// assertNotNull(savedUser);
// assertEquals("integration@test.com", savedUser.getEmail());

// // Step 2: Activate the user (simulate email verification)
// savedUser.setIsVerified(true);
// userRepository.save(savedUser);

// // Step 3: Login with the registered user
// LoginDto loginDto = new LoginDto();
// loginDto.setUsername("integrationtest");
// loginDto.setPassword("Integration@123");

// mockMvc.perform(post("/api/auth/login")
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(loginDto)))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.success").value(true))
// .andExpect(jsonPath("$.data.token").exists())
// .andExpect(jsonPath("$.data.username").value("integrationtest"));
// }

// }

// d registerUser_Success() {
// // Given
// when(userRepository.existsByUsername(anyString())).thenReturn(false);
// when(userRepository.existsByEmail(anyString())).thenReturn(false);
// when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
// when(userRepository.save(any(User.class))).thenReturn(user);

// // When
// UserResponseDto result = userService.registerUser(registrationDto);

// // Then
// assertNotNull(result);
// assertEquals("testuser", result.getUsername());
// assertEquals("test@example.com", result.getEmail());
// verify(userRepository).save(any(User.class));
// verify(auditService).logAction(eq("USER_REGISTERED"), eq("User"), eq(1L),
// anyString());
// }

// @Test
// void registerUser_UsernameExists_ThrowsException() {
// // Given
// when(userRepository.existsByUsername(anyString())).thenReturn(true);

// // When & Then
// assertThrows(UserAlreadyExistsException.class, () -> {
// userService.registerUser(registrationDto);
// });

// verify(userRepository, never()).save(any(User.class));
// }

// @Test
// void getUserById_Success() {
// // Given
// when(userRepository.findById(1L)).thenReturn(Optional.of(user));

// // When
// UserResponseDto result = userService.getUserById(1L);

// // Then
// assertNotNull(result);
// assertEquals("testuser", result.getUsername());
// }

// @Test
// void getUserById_NotFound_ThrowsException() {
// // Given
// when(userRepository.findById(1L)).thenReturn(Optional.empty());

// // When & Then
// assertThrows(UserNotFoundException.class, () -> {
// userService.getUserById(1L);
// });
// }

// @Test
// void lockUser_Success() {
// // Given
// when(userRepository.findById(1L)).thenReturn(Optional.of(user));

// // When
// userService.lockUser(1L, 24);

// // Then
// verify(userRepository).save(user);
// assertNotNull(user.getAccountLockedUntil());
// verify(auditService).logAction(eq("USER_LOCKED"), eq("User"), eq(1L),
// anyString());
// }
// }
