package project.trendpick_pro.global.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({DataProperties.class, AmazonProperties.class})
@RequiredArgsConstructor
public class AwsConfig {

    private final AmazonProperties amazonProperties;

    @Bean
    public AmazonS3Client amazonS3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                amazonProperties.getCredentials().getAccessKey(),
                amazonProperties.getCredentials().getSecretKey());
        return (AmazonS3Client) AmazonS3Client.builder()
                .withRegion(amazonProperties.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
