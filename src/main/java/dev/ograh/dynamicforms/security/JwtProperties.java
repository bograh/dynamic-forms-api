package dev.ograh.dynamicforms.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ConfigurationProperties(prefix = "security-jwt")
public class JwtProperties {

    private String accessTokenSecret;
    private long accessTokenExpirationMs;
    private String refreshTokenSecret;
    private long refreshTokenExpirationMs;

}