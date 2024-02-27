package project.trendpick_pro.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import project.trendpick_pro.global.crypto.oauth2.OAuthTokenResponse;

import java.time.LocalDateTime;

@Embeddable
@Getter
@AllArgsConstructor
public class SocialAuthToken {

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime accessTokenExpiresAt;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime refreshTokenExpiresAt;

    public void updateToken(OAuthTokenResponse oAuthTokenResponse){
        this.accessToken = oAuthTokenResponse.getAccessToken();
        this.refreshToken = oAuthTokenResponse.getRefreshToken();
        this.accessTokenExpiresAt = LocalDateTime.now().plusSeconds(oAuthTokenResponse.getExpiresIn());
        this.refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(oAuthTokenResponse.getRefreshTokenExpiresIn());
    }

    public void updateAccessToken(String accessToken, LocalDateTime accessTokenExpiresAt){
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime refreshTokenExpiresAt){
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}
