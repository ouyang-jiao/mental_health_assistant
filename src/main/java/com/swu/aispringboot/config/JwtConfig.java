package com.swu.aispringboot.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    public String secret;
    public long expiration;
    public long refreshExpiration;
    public String header;
    public String tokenPrefix;
}
