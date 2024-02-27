package project.trendpick_pro.global.crypto.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
@RequiredArgsConstructor
public class JwtProperties {

    private String secret;
    private long accessExpirationTime;
    private long refreshExpirationTime;
}
