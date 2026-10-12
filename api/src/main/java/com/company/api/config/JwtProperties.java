package com.company.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private final String secret;
    private final Duration accessTokenTtl;
}
