package com.company.api.config;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  private final String secret;
  private final Duration accessTokenTtl;
}
