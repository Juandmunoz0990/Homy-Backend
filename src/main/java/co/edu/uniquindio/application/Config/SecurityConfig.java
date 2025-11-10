package co.edu.uniquindio.application.Config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.uniquindio.application.Security.CustomUserDetailsService;
import co.edu.uniquindio.application.Security.JwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("/users/forgot-password", "/users/verify-code", "/users/reset-password").permitAll()
                            .requestMatchers("/auth/**").permitAll()
                            .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
            .httpBasic(Customizer.withDefaults())
            .build();
}

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-CSRF-Token"));
        configuration.setAllowCredentials(true); // Necesario para cookies
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeErrorResponse(request, response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "Invalid or expired JWT from API");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeErrorResponse(request, response,
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "Insufficient permissions");
    }

    private void writeErrorResponse(HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
        response.getWriter().write(json);
    }
}
