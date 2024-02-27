package project.trendpick_pro.global.crypto.jasypt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jasypt.encryptor")
public class CryptoProperties {
    private String algorithm;
    private Integer poolSize;
    private String stringOutputType;
    private Integer keyObtentionIterations;
    private String password;
}
