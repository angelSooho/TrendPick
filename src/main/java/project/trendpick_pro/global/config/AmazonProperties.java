package project.trendpick_pro.global.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cloud.aws")
public class AmazonProperties {
    @JsonProperty("s3.bucket")
    private String bucket;
    private Credentials credentials;
    @JsonProperty("region.static")
    private String region;
    private String endpoint;

    @Data
    static class Credentials {
        private String accessKey;
        private String secretKey;
    }
}
