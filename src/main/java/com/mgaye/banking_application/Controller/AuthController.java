// package com.mgaye.banking_application.Controller;

// import com.mgaye.banking_application.dto.*;
// import com.mgaye.banking_application.security.JwtHelper;
// import com.mgaye.banking_application.service.UserDetailsServiceImpl;
// import com.mgaye.banking_application.service.UserService;
// import com.mgaye.banking_application.service.AuditService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.DisabledException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

//     @PostMapping("/login")
//     public ResponseEntity<ApiResponse<JwtResponseDto>> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
//         try {
//             // Attempt authentication
//             authenticationManager.authenticate(
//                     new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

//             // Load user details
//             UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUsername());
//             UserDetailsServiceImpl.UserDetailsImpl userDetailsImpl = (UserDetailsServiceImpl.UserDetailsImpl) userDetails;

//             // Generate JWT token
//             String jwt = jwtHelper.generateToken(userDetails);

//             // Reset failed login attempts on successful login
//             userService.resetFailedLoginAttempts(loginDto.getUsername());

//             // Create response
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
//             // Increment failed login attempts
//             try {
//                 userService.incrementFailedLoginAttempts(loginDto.getUsername());
//             } catch (Exception ex) {
//                 // User might not exist
//             }

//             auditService.logAction("LOGIN_FAILED", "User", null,
//                     "Failed login attempt for username: " + loginDto.getUsername());

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
// }

package com.mgaye.banking_application.Controller;

import com.mgaye.banking_application.dto.*;
import com.mgaye.banking_application.security.JwtHelper;
import com.mgaye.banking_application.service.UserDetailsServiceImpl;
import com.mgaye.banking_application.service.UserService;
import com.mgaye.banking_application.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtHelper jwtHelper;
    private final UserService userService;
    private final AuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            UserResponseDto user = userService.registerUser(registrationDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDto>> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        try {
            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getEmail());
            UserDetailsServiceImpl.UserDetailsImpl userDetailsImpl = (UserDetailsServiceImpl.UserDetailsImpl) userDetails;

            // Generate JWT token
            String jwt = jwtHelper.generateToken(userDetails);

            // Reset failed login attempts on successful login
            userService.resetFailedLoginAttempts(loginDto.getEmail());

            // Create response
            JwtResponseDto response = new JwtResponseDto(
                    jwt,
                    userDetails.getUsername(),
                    userDetailsImpl.getUser().getEmail(),
                    userDetailsImpl.getUser().getRole().toString(),
                    userDetailsImpl.getId());

            auditService.logAction("LOGIN_SUCCESS", "User", userDetailsImpl.getId(),
                    "User logged in successfully");

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (BadCredentialsException e) {
            // Increment failed login attempts
            try {
                userService.incrementFailedLoginAttempts(loginDto.getEmail());
            } catch (Exception ex) {
                // User might not exist
            }

            auditService.logAction("LOGIN_FAILED", "User", null,
                    "Failed login attempt for username: " + loginDto.getEmail());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Account is disabled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In a real application, you might want to blacklist the token
        auditService.logAction("LOGOUT", "User", null, "User logged out");
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
