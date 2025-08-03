package com.mgaye.banking_application.service;

import com.mgaye.banking_application.dto.UserRegistrationDto;
import com.mgaye.banking_application.dto.UserResponseDto;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.entity.Role;
import com.mgaye.banking_application.exception.UserAlreadyExistsException;
import com.mgaye.banking_application.exception.UserNotFoundException;
import com.mgaye.banking_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setAddress(registrationDto.getAddress());
        user.setDateOfBirth(registrationDto.getDateOfBirth());
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);
        auditService.logAction("USER_REGISTERED", "User", savedUser.getId(),
                "New user registered: " + savedUser.getUsername());

        return convertToResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return convertToResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public void lockUser(Long userId, int hours) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountLockedUntil(LocalDateTime.now().plusHours(hours));
        userRepository.save(user);

        auditService.logAction("USER_LOCKED", "User", userId,
                "User locked for " + hours + " hours");
    }

    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        auditService.logAction("USER_UNLOCKED", "User", userId, "User unlocked");
    }

    public void incrementFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountLockedUntil(LocalDateTime.now().plusHours(24));
            auditService.logAction("USER_AUTO_LOCKED", "User", user.getId(),
                    "User auto-locked due to failed login attempts");
        }

        userRepository.save(user);
    }

    public void resetFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    private UserResponseDto convertToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setRole(user.getRole().toString());
        dto.setIsActive(user.getIsActive());
        dto.setIsVerified(user.getIsVerified());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}