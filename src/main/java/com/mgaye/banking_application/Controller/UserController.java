package com.mgaye.banking_application.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgaye.banking_application.dto.LoginAttemptDto;
import com.mgaye.banking_application.dto.SecurityAlertDto;
import com.mgaye.banking_application.dto.TrustedDeviceDto;
import com.mgaye.banking_application.dto.UserDto;
import com.mgaye.banking_application.dto.UserSessionDto;
import com.mgaye.banking_application.dto.request.UpdateProfileRequest;
import com.mgaye.banking_application.dto.response.MessageResponse;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.service.SecurityService;
import com.mgaye.banking_application.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    public UserController(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        User updatedUser = userService.updateProfile(user, request, getClientIpAddress(httpRequest));
        return ResponseEntity.ok(UserDto.from(updatedUser));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionDto>> getActiveSessions(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<UserSessionDto> sessions = userService.getActiveSessions(user);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<MessageResponse> terminateSession(
            @PathVariable String sessionId,
            HttpServletRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        userService.terminateSession(user, sessionId, getClientIpAddress(request));
        return ResponseEntity.ok(new MessageResponse("Session terminated successfully"));
    }

    @GetMapping("/trusted-devices")
    public ResponseEntity<List<TrustedDeviceDto>> getTrustedDevices(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<TrustedDeviceDto> devices = userService.getTrustedDevices(user);
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/trusted-devices/{deviceId}")
    public ResponseEntity<MessageResponse> removeTrustedDevice(
            @PathVariable Long deviceId,
            HttpServletRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        userService.removeTrustedDevice(user, deviceId, getClientIpAddress(request));
        return ResponseEntity.ok(new MessageResponse("Trusted device removed successfully"));
    }

    @GetMapping("/security-alerts")
    public ResponseEntity<List<SecurityAlertDto>> getSecurityAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        List<SecurityAlertDto> alerts = securityService.getUserAlerts(user, page, size);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/security-alerts/{alertId}/acknowledge")
    public ResponseEntity<MessageResponse> acknowledgeAlert(
            @PathVariable Long alertId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        securityService.acknowledgeAlert(user, alertId);
        return ResponseEntity.ok(new MessageResponse("Alert acknowledged"));
    }

    @GetMapping("/login-history")
    public ResponseEntity<List<LoginAttemptDto>> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        List<LoginAttemptDto> history = userService.getLoginHistory(user, page, size);
        return ResponseEntity.ok(history);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}