package com.DcoDe.jobconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

     @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // .requestMatchers(HttpMethod.POST, "/api/auth/register/candidate").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // .requestMatchers("/api/employer/**").permitAll()
                        // .requestMatchers("/api/jobs/**").permitAll()
                        // .requestMatchers("/api/files/**").permitAll()
                        // .requestMatchers("/uploads/**").permitAll()
                        // .requestMatchers("/api/candidate/**").permitAll()
                        // .requestMatchers("/api/companies/**").permitAll()
                        // .requestMatchers("/api/companies/*/jobs").permitAll()
                        // .requestMatchers("/api/companies/search").permitAll()
                        // .requestMatchers("/api/companies/{companyUniqueId}").permitAll()
                        // .requestMatchers("/api/companies/profile").permitAll() // Overridden by @PreAuthorize
                        // .requestMatchers("/api/debug/auth").permitAll()
                        // .requestMatchers("/api/candidates/search").permitAll()
                      
                        // .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {});// Enable Basic Authentication

        return http.build();
    }

            @Bean
            public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
    }
}
