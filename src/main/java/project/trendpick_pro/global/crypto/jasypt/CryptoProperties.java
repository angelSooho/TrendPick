package project.trendpick_pro.global.crypto.jasypt;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "jasypt.encryptor")
public class CryptoProperties {
    private String algorithm;
    private Integer poolSize;
    private String stringOutputType;
    private Integer keyObtentionIterations;
    private String password;
}
