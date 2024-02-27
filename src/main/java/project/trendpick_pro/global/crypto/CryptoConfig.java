package project.trendpick_pro.global.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import project.trendpick_pro.global.crypto.jasypt.CryptoProperties;
import project.trendpick_pro.global.crypto.jwt.JwtProperties;
import project.trendpick_pro.global.crypto.oauth2.OAuthClientProperties;

@Configuration
@EnableConfigurationProperties({
        CryptoProperties.class,
        JwtProperties.class,
        OAuthClientProperties.class,
})
public class CryptoConfig {}
