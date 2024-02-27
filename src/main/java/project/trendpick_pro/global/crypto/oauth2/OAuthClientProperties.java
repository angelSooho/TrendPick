package project.trendpick_pro.global.crypto.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@ConfigurationProperties(prefix = "oauth")
@RequiredArgsConstructor
public class OAuthClientProperties {

    private final String nameJson;
    private final Social kakao;
    private final Social naver;

    public String kakaoRevokeUrl() {
        return "https://kapi.kakao.com/v1/user/unlink";
    }

    public String naverRevokeUrl(String accessToken, String provider) {
        return "https://nid.naver.com/oauth2.0/token?" +
                "grant_type=delete&" +
                "client_id=" + naver.getClientId() + "&" +
                "client_secret=" + naver.getClientSecret() + "&" +
                "access_token=" + accessToken + "&" +
                "service_provider=" + provider;
    }

    @Getter
    public class Social {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
        private List<String> scope;
    }
}
