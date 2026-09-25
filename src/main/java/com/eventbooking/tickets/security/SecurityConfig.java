package com.eventbooking.tickets.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth

                        // Authentication APIs
                        .requestMatchers("/auth/**").permitAll()

                        // Swagger APIs
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/events/organizer/**")
                        .hasRole("EVENT_ORGANIZER")

                        // Anyone can view events
                        .requestMatchers(HttpMethod.GET, "/events/**")
                        .permitAll()

                        // Only EVENT_ORGANIZER can create events
                        .requestMatchers(HttpMethod.POST, "/events/**")
                        .hasRole("EVENT_ORGANIZER")

                        // Only EVENT_ORGANIZER can update events
                        .requestMatchers(HttpMethod.PUT, "/events/**")
                        .hasRole("EVENT_ORGANIZER")

                        // Only EVENT_ORGANIZER can delete events
                        .requestMatchers(HttpMethod.DELETE, "/events/**")
                        .hasRole("EVENT_ORGANIZER")

                        // Organizers can only request statistics scoped to their account
                        .requestMatchers(HttpMethod.GET, "/bookings/organizer/statistics")
                        .hasRole("EVENT_ORGANIZER")

                        // Only CUSTOMER can access bookings
                        .requestMatchers("/bookings/**")
                        .hasRole("CUSTOMER")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}