package project.trendpick_pro.global.crypto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.trendpick_pro.domain.member.entity.SocialProvider;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OAuth2Attribute {

    private Map<String, Object> attributes;
    private String attributeKey;
    private String provider;
    private String email;
    private String nickName;
//    private String phoneNumber;

    public static OAuth2Attribute of(SocialProvider providerType, Map<String, Object> attributes, String nickName) {
        return switch (providerType) {
            case KAKAO -> ofKakao(providerType.getValue(), "email", attributes, nickName);
            case NAVER -> ofNaver(providerType.getValue(), "response", attributes, nickName);
            case GOOGLE -> ofGoogle(providerType.getValue(), "id", attributes, nickName);
        };
    }

    private static OAuth2Attribute ofKakao(String provider, String attributeKey, Map<String, Object> attributes, String randomNickName) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String email = (String) kakaoAccount.get("email");
        String nickName = profile != null ? (String) profile.get("nickname") : randomNickName;

        if (email == null) {
            throw new IllegalArgumentException("email이 존재하지 않습니다.");
        }

        return new OAuth2Attribute(kakaoAccount, attributeKey, provider, email, nickName);
    }

    private static OAuth2Attribute ofNaver(String provider, String attributeKey, Map<String, Object> attributes, String randomNickName) {
        Map<String, Object> response = (Map<String, Object>) attributes.get(attributeKey);
        String email = (String) response.get("email");
        String nickName = (String) response.getOrDefault("nickname", randomNickName);

        if (email == null) {
            throw new IllegalArgumentException("email이 존재하지 않습니다.");
        }

        return new OAuth2Attribute(response, attributeKey, provider, email, nickName);
    }

    private static OAuth2Attribute ofGoogle(String provider, String attributeKey, Map<String, Object> attributes, String randomNickName) {
        String email = (String) attributes.get("email");
        String nickName = (String) attributes.getOrDefault("name", randomNickName);

        if (email == null) {
            throw new IllegalArgumentException("email이 존재하지 않습니다.");
        }

        return new OAuth2Attribute(attributes, attributeKey, provider, email, nickName);
    }

    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", attributeKey);
        map.put("key", attributeKey);
        map.put("email", email);
        map.put("nickName", nickName);
        map.put("provider", provider);
        return map;
    }
}