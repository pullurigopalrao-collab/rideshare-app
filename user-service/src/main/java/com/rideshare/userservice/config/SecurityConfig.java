package com.rideshare.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disable CSRF for API testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users").permitAll() // allow registration
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // temporary basic auth for other endpoints

        return http.build();
    }
}
