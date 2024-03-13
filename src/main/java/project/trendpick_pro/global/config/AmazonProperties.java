package project.trendpick_pro.global.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cloud.aws")
public class AmazonProperties {
    private S3 s3;
    private Credentials credentials;
    private String region;
    private String endpoint;

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class S3 {
        private String bucket;
    }
}
