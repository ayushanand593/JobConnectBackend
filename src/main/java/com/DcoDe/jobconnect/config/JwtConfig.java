package com.DcoDe.jobconnect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "application.jwt")
@Data
public class JwtConfig {
    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}