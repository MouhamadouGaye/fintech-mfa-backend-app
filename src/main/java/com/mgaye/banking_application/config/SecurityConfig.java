// package com.mgaye.banking_application.config;

// import com.mgaye.banking_application.security.JwtAuthenticationEntryPoint;
// import com.mgaye.banking_application.security.JwtAuthenticationFilter;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.Arrays;

// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true)
// public class SecurityConfig {

//     @Autowired
//     private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

//     @Autowired
//     private JwtAuthenticationFilter jwtAuthenticationFilter;

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder(12);
//     }

//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//         return config.getAuthenticationManager();
//     }

//     // @Bean
//     // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//     // http.csrf(csrf -> csrf.disable())
//     // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//     // .authorizeHttpRequests(auth -> auth
//     // .requestMatchers("/api/auth/**").permitAll()
//     // .requestMatchers("/api/admin/**").hasRole("ADMIN")
//     // .requestMatchers("/api/accounts/**").hasAnyRole("CUSTOMER", "ADMIN")
//     // .requestMatchers("/api/transactions/**").hasAnyRole("CUSTOMER", "ADMIN")
//     // .requestMatchers("/api/transfers/**").hasRole("CUSTOMER")
//     // .anyRequest().authenticated())
//     // .exceptionHandling(ex ->
//     // ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//     // .sessionManagement(session ->
//     // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//     // .headers(headers -> headers.frameOptions().disable()); // For H2 console

//     // http.addFilterBefore(jwtAuthenticationFilter,
//     // UsernamePasswordAuthenticationFilter.class);

//     // return http.build();
//     // }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http.csrf(csrf -> csrf.disable())
//                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
//                         .requestMatchers("/api/auth/me").authenticated()
//                         .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                         .requestMatchers("/api/accounts/**").hasAnyRole("CUSTOMER", "ADMIN")
//                         .requestMatchers("/api/transactions/**").hasAnyRole("CUSTOMER", "ADMIN")
//                         .requestMatchers("/api/transfers/**").hasRole("CUSTOMER")
//                         .anyRequest().authenticated())
//                 .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                 .headers(headers -> headers.frameOptions().disable()); // For H2 console

//         http.addFilterBefore(jwtAuthenticationFilter,
//                 UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     // this Configure is the same as the filterChain but written more simpler
//     // @Override
//     // protected void configure(HttpSecurity http) throws Exception {
//     // http
//     // .csrf().disable()
//     // .authorizeHttpRequests()
//     // .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
//     // .requestMatchers("/api/auth/me").authenticated() // ✅ protected
//     // .anyRequest().authenticated()
//     // .and()
//     // .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//     // // 🔑 stateless
//     // .and()
//     // .addFilterBefore(jwtAuthenticationFilter,
//     // UsernamePasswordAuthenticationFilter.class);
//     // }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();
//         // configuration.setAllowedOriginPatterns(Arrays.asList("*"));
//         configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:5000")); // your frontend origin
//         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//         configuration.setAllowedHeaders(Arrays.asList("*"));
//         configuration.setAllowCredentials(true);
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
//         return source;
//     }
// }

package com.mgaye.banking_application.config;

import com.mgaye.banking_application.security.JwtAuthenticationEntryPoint;
import com.mgaye.banking_application.security.JwtAuthenticationFilter;
import com.mgaye.banking_application.service.AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strong hashing
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/home", "/api/auth/login", "/api/auth/register", "/api/auth/verify-email")
                        .permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/compliance/**").hasRole("COMPLIANCE_OFFICER")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions().deny()
                        .contentTypeOptions().and()
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("https://*.bankingapp.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}