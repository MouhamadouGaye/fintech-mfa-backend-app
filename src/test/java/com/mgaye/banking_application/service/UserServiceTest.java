// package com.mgaye.banking_application.service;

// import com.mgaye.banking_application.dto.UserRegistrationDto;
// import com.mgaye.banking_application.dto.UserResponseDto;
// import com.mgaye.banking_application.entity.Role;
// import com.mgaye.banking_application.entity.User;
// import com.mgaye.banking_application.exception.UserAlreadyExistsException;
// import com.mgaye.banking_application.exception.UserNotFoundException;
// import com.mgaye.banking_application.repository.UserRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import java.time.LocalDateTime;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class UserServiceTest {

// @Mock
// private UserRepository userRepository;

// @Mock
// private PasswordEncoder passwordEncoder;

// @Mock
// private AuditService auditService;

// @InjectMocks
// private UserService userService;

// private UserRegistrationDto registrationDto;
// private User user;

// @BeforeEach
// void setUp() {
// registrationDto = new UserRegistrationDto();
// registrationDto.setUsername("testuser");
// registrationDto.setEmail("test@example.com");
// registrationDto.setPassword("Password@123");
// registrationDto.setFirstName("Test");
// registrationDto.setLastName("User");
// registrationDto.setPhoneNumber("+1234567890");

// user = new User();
// user.setId(1L);
// user.setUsername("testuser");
// user.setEmail("test@example.com");
// user.setPassword("encodedPassword");
// user.setFirstName("Test");
// user.setLastName("User");
// user.setRole(Role.CUSTOMER);
// user.setIsActive(true);
// user.setIsVerified(false);
// user.setCreatedAt(LocalDateTime.now());
// }

// @Test
// void registerUser_Success() {
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