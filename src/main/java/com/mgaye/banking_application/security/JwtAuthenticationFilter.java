// package com.mgaye.banking_application.security;

// import com.mgaye.banking_application.service.UserDetailsServiceImpl;
// import io.jsonwebtoken.ExpiredJwtException;
// import io.jsonwebtoken.MalformedJwtException;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// @Component
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

//     @Autowired
//     private UserDetailsServiceImpl userDetailsService;

//     @Autowired
//     private JwtHelper jwtHelper;

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain) throws ServletException, IOException {

//         String requestHeader = request.getHeader("Authorization");
//         String username = null;
//         String token = null;

//         if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
//             token = requestHeader.substring(7);
//             try {
//                 username = this.jwtHelper.getUsernameFromToken(token); // this is the email
//             } catch (IllegalArgumentException e) {
//                 logger.error("Illegal Argument while fetching the username !!");
//             } catch (ExpiredJwtException e) {
//                 logger.error("Given jwt token is expired !!");
//             } catch (MalformedJwtException e) {
//                 logger.error("Some changes has done in token !! Invalid Token");
//             } catch (Exception e) {
//                 logger.error("JWT Token validation error: " + e.getMessage());
//             }
//         }

//         if (username != null &&
//                 SecurityContextHolder.getContext().getAuthentication() == null) {
//             UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//             Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
//             if (validateToken) {
//                 UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                         userDetails, null, userDetails.getAuthorities());
//                 authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                 SecurityContextHolder.getContext().setAuthentication(authentication);
//             }
//         }

//         filterChain.doFilter(request, response);
//     }

// @Override
// protected void doFilterInternal(HttpServletRequest request,
// HttpServletResponse response,
// FilterChain filterChain) throws ServletException, IOException {

// String token = null;
// String username = null;

// // First, try to get JWT from Authorization header
// String authHeader = request.getHeader("Authorization");
// if (authHeader != null && authHeader.startsWith("Bearer ")) {
// token = authHeader.substring(7);
// }

// // If not in header, check HttpOnly cookie
// if (token == null && request.getCookies() != null) {
// for (Cookie cookie : request.getCookies()) {
// if ("jwt".equals(cookie.getName())) {
// token = cookie.getValue();
// break;
// }
// }
// }

// try {
// if (token != null) {
// username = jwtHelper.getUsernameFromToken(token);
// }

// if (username != null &&
// SecurityContextHolder.getContext().getAuthentication() == null) {
// UserDetails userDetails = userDetailsService.loadUserByUsername(username);
// if (jwtHelper.validateToken(token, userDetails)) {
// UsernamePasswordAuthenticationToken authentication = new
// UsernamePasswordAuthenticationToken(
// userDetails, null, userDetails.getAuthorities());
// authentication.setDetails(new
// WebAuthenticationDetailsSource().buildDetails(request));
// SecurityContextHolder.getContext().setAuthentication(authentication);
// }
// }
// } catch (ExpiredJwtException e) {
// logger.error("JWT token is expired");
// } catch (MalformedJwtException e) {
// logger.error("Invalid JWT token");
// } catch (Exception e) {
// logger.error("Error while validating token: " + e.getMessage());
// }

// filterChain.doFilter(request, response);
// }

// @Override
// protected void doFilterInternal(HttpServletRequest request,
// HttpServletResponse response,
// FilterChain filterChain) throws ServletException, IOException {

// String path = request.getRequestURI();

// // Skip JWT processing for public endpoints
// if ("/api/auth/register".equals(path) || "/api/auth/login".equals(path)) {
// filterChain.doFilter(request, response);
// return;
// }

// String token = null;
// String username = null;

// // First, try to get JWT from Authorization header
// String authHeader = request.getHeader("Authorization");
// if (authHeader != null && authHeader.startsWith("Bearer ")) {
// token = authHeader.substring(7);
// }

// // If not in header, check HttpOnly cookie
// if (token == null && request.getCookies() != null) {
// for (Cookie cookie : request.getCookies()) {
// if ("jwt".equals(cookie.getName())) {
// token = cookie.getValue();
// break;
// }
// }
// }

// try {
// if (token != null) {
// username = jwtHelper.getUsernameFromToken(token);
// }

// if (username != null &&
// SecurityContextHolder.getContext().getAuthentication() == null) {
// UserDetails userDetails = userDetailsService.loadUserByUsername(username);
// if (jwtHelper.validateToken(token, userDetails)) {
// UsernamePasswordAuthenticationToken authentication = new
// UsernamePasswordAuthenticationToken(
// userDetails, null, userDetails.getAuthorities());
// authentication.setDetails(new
// WebAuthenticationDetailsSource().buildDetails(request));
// SecurityContextHolder.getContext().setAuthentication(authentication);
// }
// }
// } catch (ExpiredJwtException e) {
// logger.error("JWT token is expired");
// } catch (MalformedJwtException e) {
// logger.error("Invalid JWT token");
// } catch (Exception e) {
// logger.error("Error while validating token: " + e.getMessage());
// }

// filterChain.doFilter(request, response);
// }
// }

package com.mgaye.banking_application.security;

import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.repository.UserRepository;
import com.mgaye.banking_application.service.SessionService;
import com.mgaye.banking_application.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String email = tokenProvider.getEmailFromToken(token);

            Optional<User> userOpt = userRepository.findActiveVerifiedUserByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Additional security checks
                if (!user.isEnabled()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                // Update session activity
                String sessionId = request.getHeader("X-Session-ID");
                if (sessionId != null) {
                    sessionService.updateSessionActivity(sessionId);
                }

                // Create authentication
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                        authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}