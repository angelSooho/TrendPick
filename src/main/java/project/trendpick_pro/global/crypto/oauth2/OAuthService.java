package project.trendpick_pro.global.crypto.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import project.trendpick_pro.domain.member.entity.SocialAuthToken;
import project.trendpick_pro.domain.member.entity.SocialProvider;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthClientProperties oAuth2Properties;
    private final RestTemplate restTemplate;

    public OAuthTokenResponse getSocialToken(String code, SocialProvider provider, String state) {
        OAuthClientProperties.Social property = getSocialProperty(provider.getValue());
        return WebClient.create()
                .post()
                .uri(property.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(tokenRequest(code, property, provider, state))
                .retrieve()
                .bodyToMono(OAuthTokenResponse.class)
                .block();
    }

    public Map<String, Object> getSocialUserInfo(SocialProvider provider, OAuthTokenResponse tokenResponse, String state) {
        Map<String, Object> map;
        switch (provider) {
            case KAKAO -> {
                map = WebClient.create()
                        .get()
                        .uri(oAuth2Properties.getKakao().getUserInfoUri())
                        .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
            }
            case NAVER -> {
                map = WebClient.create()
                        .get()
                        .uri(oAuth2Properties.getNaver().getUserInfoUri())
                        .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }
        return map;
    }

    public void sendRevokeRequest(SocialAuthToken socialAuthToken, SocialProvider provider) {
        SocialAuthToken reissueAuthToken = verifyAndReissueSocialToken(socialAuthToken, provider);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String revokeUrl = switch (provider) {
            case KAKAO -> {
                httpHeaders.setBearerAuth(reissueAuthToken.getAccessToken());
                yield oAuth2Properties.kakaoRevokeUrl();
            }
            case NAVER -> oAuth2Properties.naverRevokeUrl(reissueAuthToken.getAccessToken(), provider.getValue());
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        };

        HttpEntity<String> httpEntity = new HttpEntity<>(revokeUrl, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(revokeUrl, HttpMethod.POST, httpEntity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("토큰 해지에 실패했습니다.");
        }
    }

    public SocialAuthToken verifyAndReissueSocialToken(SocialAuthToken socialAuthToken, SocialProvider provider) {
        LocalDateTime now = LocalDateTime.now();
        OAuthTokenResponse response;

        if (now.isAfter(socialAuthToken.getAccessTokenExpiresAt())) {
            OAuthClientProperties.Social property = getSocialProperty(provider.getValue());
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", property.getClientId());
            formData.add("refresh_token", socialAuthToken.getRefreshToken());


            response = WebClient.create()
                    .post()
                    .uri(property.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(OAuthTokenResponse.class)
                    .block();

            socialAuthToken.updateAccessToken(response.getAccessToken(), now.plusSeconds(response.getExpiresIn()));
            if (provider != SocialProvider.NAVER && now.isAfter(socialAuthToken.getRefreshTokenExpiresAt().minusDays(30))) {
                socialAuthToken.updateRefreshToken(response.getRefreshToken(), now.plusSeconds(response.getRefreshTokenExpiresIn()));
            }
        }

        return socialAuthToken;
    }

    private OAuthClientProperties.Social getSocialProperty(String provider) {
        return switch (provider) {
            case "kakao" -> oAuth2Properties.getKakao();
            case "naver" -> oAuth2Properties.getNaver();
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        };
    }

    private MultiValueMap<String, String> tokenRequest(String code, OAuthClientProperties.Social property, SocialProvider provider, String state) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        switch (provider) {
            case KAKAO -> {
                formData.add("grant_type", "authorization_code");
                formData.add("client_id", property.getClientId());
                formData.add("redirect_uri", property.getRedirectUri());
                formData.add("code", code);
            }
            case NAVER -> {
                formData.add("grant_type", "authorization_code");
                formData.add("client_id", property.getClientId());
                formData.add("client_secret", property.getClientSecret());
                formData.add("code", code);
                formData.add("state", state);
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }
        return formData;
    }
}
