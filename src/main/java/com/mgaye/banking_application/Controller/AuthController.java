// package com.mgaye.banking_application.Controller;

// import com.mgaye.banking_application.dto.*;
// import com.mgaye.banking_application.security.JwtHelper;
// import com.mgaye.banking_application.service.UserDetailsServiceImpl;
// import com.mgaye.banking_application.service.UserDetailsServiceImpl.UserDetailsImpl;
// import com.mgaye.banking_application.service.UserService;
// import com.mgaye.banking_application.service.AuditService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// import java.util.Collection;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.DisabledException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*", maxAge = 3600)
// public class AuthController {

//     private final AuthenticationManager authenticationManager;
//     private final UserDetailsServiceImpl userDetailsService;
//     private final JwtHelper jwtHelper;
//     private final UserService userService;
//     private final AuditService auditService;

//     @PostMapping("/register")
//     public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
//             @Valid @RequestBody UserRegistrationDto registrationDto) {
//         try {
//             UserResponseDto user = userService.registerUser(registrationDto);
//             return ResponseEntity.status(HttpStatus.CREATED)
//                     .body(ApiResponse.success("User registered successfully", user));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                     .body(ApiResponse.error(e.getMessage()));
//         }
//     }

//     // @PostMapping("/login")
//     // public ResponseEntity<ApiResponse<JwtResponseDto>> authenticateUser(@Valid
//     // @RequestBody LoginDto loginDto) {
//     // try {
//     // // Attempt authentication
//     // authenticationManager.authenticate(
//     // new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
//     // loginDto.getPassword()));

//     // // Load user details
//     // UserDetails userDetails =
//     // userDetailsService.loadUserByUsername(loginDto.getEmail());
//     // UserDetailsServiceImpl.UserDetailsImpl userDetailsImpl =
//     // (UserDetailsServiceImpl.UserDetailsImpl) userDetails;

//     // // Generate JWT token
//     // String jwt = jwtHelper.generateToken(userDetails);

//     // // Reset failed login attempts on successful login
//     // userService.resetFailedLoginAttempts(loginDto.getEmail());

//     // // Create response
//     // JwtResponseDto response = new JwtResponseDto(
//     // jwt,
//     // userDetails.getUsername(),
//     // userDetailsImpl.getUser().getEmail(),
//     // userDetailsImpl.getUser().getRole().toString(),
//     // userDetailsImpl.getId());

//     // auditService.logAction("LOGIN_SUCCESS", "User", userDetailsImpl.getId(),
//     // "User logged in successfully");

//     // return ResponseEntity.ok(ApiResponse.success("Login successful", response));

//     // } catch (BadCredentialsException e) {
//     // // Increment failed login attempts
//     // try {
//     // userService.incrementFailedLoginAttempts(loginDto.getEmail());
//     // } catch (Exception ex) {
//     // // User might not exist
//     // }

//     // auditService.logAction("LOGIN_FAILED", "User", null,
//     // "Failed login attempt for username: " + loginDto.getEmail());

//     // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//     // .body(ApiResponse.error("Invalid credentials"));

//     // } catch (DisabledException e) {
//     // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//     // .body(ApiResponse.error("Account is disabled"));
//     // } catch (Exception e) {
//     // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//     // .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
//     // }
//     // }

//     // AuthController.java (relevant login method only)
//     @PostMapping("/login")
//     public ResponseEntity<ApiResponse<JwtResponseDto>> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
//         try {
//             authenticationManager.authenticate(
//                     new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

//             UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getEmail());
//             UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;

//             // Check if 2FA enabled
//             if (userDetailsImpl.getUser().isTwoFactorEnabled()) {
//                 String totpCode = loginDto.getTotpCode();
//                 if (totpCode == null || !totpService.verifyCode(userDetailsImpl.getUser().getMfaSecret(), totpCode)) {
//                     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                             .body(ApiResponse.error("Invalid or missing TOTP code"));
//                 }
//             }

//             String jwt = jwtHelper.generateToken(userDetails);

//             userService.resetFailedLoginAttempts(loginDto.getEmail());

//             JwtResponseDto response = new JwtResponseDto(
//                     jwt,
//                     userDetails.getUsername(),
//                     userDetailsImpl.getUser().getEmail(),
//                     userDetailsImpl.getUser().getRole().toString(),
//                     userDetailsImpl.getId());

//             auditService.logAction("LOGIN_SUCCESS", "User", userDetailsImpl.getId(),
//                     "User logged in successfully");

//             return ResponseEntity.ok(ApiResponse.success("Login successful", response));

//         } catch (BadCredentialsException e) {
//             userService.incrementFailedLoginAttempts(loginDto.getEmail());
//             auditService.logAction("LOGIN_FAILED", "User", null,
//                     "Failed login attempt for username: " + loginDto.getEmail());
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(ApiResponse.error("Invalid credentials"));

//         } catch (DisabledException e) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(ApiResponse.error("Account is disabled"));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
//         }
//     }

//     @PostMapping("/logout")
//     public ResponseEntity<ApiResponse<String>> logout() {
//         // In a real application, you might want to blacklist the token
//         auditService.logAction("LOGOUT", "User", null, "User logged out");
//         return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
//     }

//     @GetMapping("/me")
//     @PreAuthorize("isAuthenticated()")
//     public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(Authentication authentication) {
//         try {
//             // The authentication object should contain the current user's principal
//             UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//             UserResponseDto user = userService.getUserById(userDetails.getId());
//             return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
//         }
//     }
// }

// package com.mgaye.banking_application.Controller;

// import com.mgaye.banking_application.dto.*;
// import com.mgaye.banking_application.security.JwtHelper;
// import com.mgaye.banking_application.service.UserDetailsServiceImpl;
// import
// com.mgaye.banking_application.service.UserDetailsServiceImpl.UserDetailsImpl;
// import com.mgaye.banking_application.service.UserService;
// import com.mgaye.banking_application.service.AuditService;

// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// import java.util.Collection;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.DisabledException;
// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*", maxAge = 3600)
// public class AuthController {

// private final AuthenticationManager authenticationManager;
// private final UserDetailsServiceImpl userDetailsService;
// private final JwtHelper jwtHelper;
// private final UserService userService;
// private final AuditService auditService;

// @PostMapping("/register")
// public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
// @Valid @RequestBody UserRegistrationDto registrationDto) {
// try {

// UserResponseDto user = userService.registerUser(registrationDto);
// return ResponseEntity.status(HttpStatus.CREATED)
// .body(ApiResponse.success("User registered successfully", user));
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.BAD_REQUEST)
// .body(ApiResponse.error(e.getMessage()));
// }
// }

// // @PostMapping("/register")
// // public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
// // @Valid @RequestBody UserRegistrationDto registrationDto,
// // HttpServletResponse response) {
// // try {
// // // 1. Register the user
// // UserResponseDto user = userService.registerUser(registrationDto);

// // // 2. Authenticate the new user
// // authenticationManager.authenticate(
// // new UsernamePasswordAuthenticationToken(
// // registrationDto.getEmail(),
// // registrationDto.getPassword()));

// // UserDetails userDetails =
// // userDetailsService.loadUserByUsername(registrationDto.getEmail());
// // UserDetailsServiceImpl.UserDetailsImpl userDetailsImpl =
// // (UserDetailsServiceImpl.UserDetailsImpl) userDetails;

// // // 3. Generate JWT
// // String jwt = jwtHelper.generateToken(userDetails);

// // // 4. Set JWT as HttpOnly cookie
// // Cookie cookie = new Cookie("jwt", jwt);
// // cookie.setHttpOnly(true);
// // cookie.setSecure(true); // ⚠️ Only use true in production (HTTPS)
// // cookie.setPath("/");
// // cookie.setMaxAge(24 * 60 * 60); // 1 day
// // cookie.setAttribute("SameSite", "Lax");
// // response.addCookie(cookie);

// // // 5. Log and respond
// // auditService.logAction("USER_REGISTERED", "User", userDetailsImpl.getId(),
// // "New user registered and logged in: " + userDetailsImpl.getUsername());

// // return ResponseEntity.status(HttpStatus.CREATED)
// // .body(ApiResponse.success("User registered and logged in successfully",
// // user));

// // } catch (Exception e) {
// // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
// // .body(ApiResponse.error(e.getMessage()));
// // }
// // }

// // @PostMapping("/register")
// // public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
// // @Valid @RequestBody UserRegistrationDto registrationDto,
// // HttpServletResponse response) {
// // try {
// // // 1. Register user
// // UserResponseDto user = userService.registerUser(registrationDto);

// // // 2. Authenticate
// // authenticationManager.authenticate(
// // new UsernamePasswordAuthenticationToken(
// // registrationDto.getEmail(),
// // registrationDto.getPassword()));

// // // 3. Load user details
// // UserDetails userDetails =
// // userDetailsService.loadUserByUsername(registrationDto.getEmail());

// // // 4. Generate JWT
// // String jwt = jwtHelper.generateToken(userDetails);

// // // 5. Set JWT as cookie
// // Cookie cookie = new Cookie("jwt", jwt);
// // cookie.setHttpOnly(true);
// // cookie.setSecure(false); // false for localhost
// // cookie.setPath("/");
// // cookie.setMaxAge(24 * 60 * 60);
// // cookie.setAttribute("SameSite", "Lax");
// // response.addCookie(cookie);

// // // 6. Log audit (only once)
// // UserDetailsServiceImpl.UserDetailsImpl impl =
// // (UserDetailsServiceImpl.UserDetailsImpl) userDetails;
// // auditService.logAction("USER_REGISTERED", "User", impl.getId(),
// // "New user registered and logged in: " + impl.getUsername());

// // return ResponseEntity.status(HttpStatus.CREATED)
// // .body(ApiResponse.success("User registered and logged in successfully",
// // user));

// // } catch (Exception e) {
// // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
// // .body(ApiResponse.error(e.getMessage()));
// // }
// // }

// @PostMapping("/login")
// public ResponseEntity<ApiResponse<JwtResponseDto>> authenticateUser(@Valid
// @RequestBody LoginDto loginDto) {
// try {
// // Attempt authentication
// authenticationManager.authenticate(
// new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
// loginDto.getPassword()));

// // Load user details
// UserDetails userDetails =
// userDetailsService.loadUserByUsername(loginDto.getEmail());
// UserDetailsServiceImpl.UserDetailsImpl userDetailsImpl =
// (UserDetailsServiceImpl.UserDetailsImpl) userDetails;

// // Generate JWT token
// String jwt = jwtHelper.generateToken(userDetails);

// // Reset failed login attempts on successful login
// userService.resetFailedLoginAttempts(loginDto.getEmail());

// // Create response
// JwtResponseDto response = new JwtResponseDto(
// jwt,
// userDetails.getUsername(),
// // userDetailsImpl.getUser().getEmail(),
// userDetailsImpl.getUser().getRole().toString(),
// userDetailsImpl.getId());

// auditService.logAction("LOGIN_SUCCESS", "User", userDetailsImpl.getId(),
// "User logged in successfully");

// return ResponseEntity.ok(ApiResponse.success("Login successful", response));

// } catch (BadCredentialsException e) {
// // Increment failed login attempts
// try {
// userService.incrementFailedLoginAttempts(loginDto.getEmail());
// } catch (Exception ex) {
// // User might not exist
// }

// auditService.logAction("LOGIN_FAILED", "User", null,
// "Failed login attempt for username: " + loginDto.getEmail());

// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(ApiResponse.error("Invalid credentials"));

// } catch (DisabledException e) {
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(ApiResponse.error("Account is disabled"));
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
// }
// }

// // @PostMapping("/login")
// // public ResponseEntity<ApiResponse<UserResponseDto>> authenticateUser(
// // @Valid @RequestBody LoginDto loginDto,
// // HttpServletResponse response) {
// // try {
// // // 1. Authenticate
// // authenticationManager.authenticate(
// // new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
// // loginDto.getPassword()));

// // // 2. Load user details
// // UserDetails userDetails =
// // userDetailsService.loadUserByUsername(loginDto.getEmail());
// // UserDetailsServiceImpl.UserDetailsImpl userDetailsImpl =
// // (UserDetailsServiceImpl.UserDetailsImpl) userDetails;

// // // 3. Generate JWT
// // String jwt = jwtHelper.generateToken(userDetails);

// // // 4. Reset failed login attempts
// // userService.resetFailedLoginAttempts(loginDto.getEmail());

// // // 5. Set JWT as HttpOnly cookie
// // Cookie cookie = new Cookie("jwt", jwt);
// // cookie.setHttpOnly(true);
// // cookie.setSecure(true); // ⚠️ Only enable this in production with HTTPS
// // cookie.setPath("/");
// // cookie.setMaxAge(24 * 60 * 60); // 1 day
// // cookie.setAttribute("SameSite", "Lax"); // Or "Strict" for more security
// // response.addCookie(cookie);

// // // 6. Build response (excluding JWT)
// // UserResponseDto userResponse =
// // UserResponseDto.fromUser(userDetailsImpl.getUser());
// // auditService.logAction("LOGIN_SUCCESS", "User", userDetailsImpl.getId(),
// // "User logged in successfully");

// // return ResponseEntity.ok(ApiResponse.success("Login successful",
// // userResponse));

// // } catch (BadCredentialsException e) {
// // try {
// // userService.incrementFailedLoginAttempts(loginDto.getEmail());
// // } catch (Exception ignored) {
// // }

// // auditService.logAction("LOGIN_FAILED", "User", null,
// // "Failed login attempt for username: " + loginDto.getEmail());

// // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// // .body(ApiResponse.error("Invalid credentials"));

// // } catch (DisabledException e) {
// // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// // .body(ApiResponse.error("Account is disabled"));
// // } catch (Exception e) {
// // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// // .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
// // }
// // }

// @PostMapping("/logout")
// public ResponseEntity<ApiResponse<String>> logout() {
// // In a real application, you might want to blacklist the token
// auditService.logAction("LOGOUT", "User", null, "User logged out");
// return ResponseEntity.ok(ApiResponse.success("Logged out successfully",
// null));
// }

// @GetMapping("/me")
// public ResponseEntity<UserResponseDto> getLoggedInUser() {
// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
// String username = auth.getName(); // typically the username
// Collection<? extends GrantedAuthority> roles = auth.getAuthorities();

// // Use the username to look up user details from DB, etc.
// return ResponseEntity.ok(userService.getUserByEmail(username));
// }
// }

package com.mgaye.banking_application.Controller;

import com.mgaye.banking_application.dto.*;
import com.mgaye.banking_application.dto.request.AuthenticationRequest;
import com.mgaye.banking_application.dto.request.ChangePasswordRequest;
import com.mgaye.banking_application.dto.request.ForgotPasswordRequest;
import com.mgaye.banking_application.dto.request.MfaSetupRequest;
import com.mgaye.banking_application.dto.request.RefreshTokenRequest;
import com.mgaye.banking_application.dto.request.RegistrationRequest;
import com.mgaye.banking_application.dto.request.ResetPasswordRequest;
import com.mgaye.banking_application.dto.request.VerifyMfaSetupRequest;
import com.mgaye.banking_application.dto.response.AuthenticationResponse;
import com.mgaye.banking_application.dto.response.BackupCodesResponse;
import com.mgaye.banking_application.dto.response.MessageResponse;
import com.mgaye.banking_application.dto.response.MfaSetupResponse;
import com.mgaye.banking_application.dto.response.RegistrationResponse;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.security.JwtHelper;
import com.mgaye.banking_application.service.UserDetailsServiceImpl;
import com.mgaye.banking_application.service.UserDetailsServiceImpl.UserDetailsImpl;
import com.mgaye.banking_application.service.UserService;
import com.mgaye.banking_application.service.AuditService;
import com.mgaye.banking_application.service.AuthenticationService;
import com.mgaye.banking_application.service.MfaService;
import com.mgaye.banking_application.service.PasswordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;
    private final MfaService mfaService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest) {

        AuthenticationResponse response = authenticationService.authenticate(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest) {

        RegistrationResponse response = authenticationService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @RequestParam String token,
            HttpServletRequest request) {

        userService.verifyEmail(token, getClientIpAddress(request));
        return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        userService.initiatePasswordReset(request.getEmail(), getClientIpAddress(httpRequest));
        return ResponseEntity.ok(new MessageResponse("Password reset instructions sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        userService.resetPassword(request.getToken(), request.getNewPassword(),
                getClientIpAddress(httpRequest));
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        userService.changePassword(user, request.getCurrentPassword(),
                request.getNewPassword(), getClientIpAddress(httpRequest));
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthenticationResponse response = authenticationService.refreshToken(
                request.getRefreshToken(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout(
            HttpServletRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String sessionId = request.getHeader("X-Session-ID");

        authenticationService.logout(user, sessionId, getClientIpAddress(request));
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logoutAll(
            HttpServletRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        authenticationService.logoutAllSessions(user, getClientIpAddress(request));
        return ResponseEntity.ok(new MessageResponse("Logged out from all devices successfully"));
    }

    // ========== MFA ENDPOINTS ==========

    @PostMapping("/mfa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaSetupResponse> setupMfa(
            @Valid @RequestBody MfaSetupRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        MfaSetupResponse response = mfaService.setupMfa(user, request.getMethod());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa/verify-setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> verifyMfaSetup(
            @Valid @RequestBody VerifyMfaSetupRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        mfaService.verifyAndEnableMfa(user, request.getCode(), request.getBackupCodes(),
                getClientIpAddress(httpRequest));
        return ResponseEntity.ok(new MessageResponse("MFA enabled successfully"));
    }

    // @PostMapping("/mfa/disable")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<MessageResponse> disableMfa(
    // @Valid @RequestBody DisableMfaRequest request,
    // HttpServletRequest httpRequest,
    // Authentication authentication) {

    // User user = (User) authentication.getPrincipal();
    // mfaService.disableMfa(user, request.getPassword(),
    // getClientIpAddress(httpRequest));
    // return ResponseEntity.ok(new MessageResponse("MFA disabled successfully"));
    // }

    // @GetMapping("/mfa/backup-codes")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<BackupCodesResponse> getBackupCodes(Authentication
    // authentication) {
    // User user = (User) authentication.getPrincipal();
    // List<String> codes = mfaService.generateBackupCodes(user);
    // return ResponseEntity.ok(new BackupCodesResponse(codes));
    // }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}