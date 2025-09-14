package com.finance.finance.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/auth/health", "/auth/", "/error").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics", "/actuator/prometheus").permitAll()
                .requestMatchers("/auth/categories/initialize", "/auth/categories/stats", "/auth/test/**").permitAll()
                .requestMatchers("/auth/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic
                .realmName("Finance API")
            );

        return http.build();
    }
}
