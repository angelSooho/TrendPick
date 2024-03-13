package project.trendpick_pro.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.trendpick_pro.domain.common.base.BaseTimeEntity;
import project.trendpick_pro.domain.tags.favoritetag.entity.FavoriteTag;
import project.trendpick_pro.domain.tags.tag.entity.TagType;
import project.trendpick_pro.global.crypto.oauth2.OAuthTokenResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String phoneNumber;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Embedded
    private SocialAuthToken socialAuthToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    private String brand;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private Set<FavoriteTag> tags = new LinkedHashSet<>();

    @Builder
    private Member(String email, String nickName, String phoneNumber,
                   SocialProvider provider, MemberRole role, String brand) {
        this.email = email;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.role = role;
        this.brand = brand;
    }

    public void connectSocialAuthToken(OAuthTokenResponse socialAuthToken) {
        this.socialAuthToken = new SocialAuthToken(
                socialAuthToken.getAccessToken(),
                socialAuthToken.getRefreshToken(),
                LocalDateTime.now().plusSeconds(socialAuthToken.getExpiresIn()),
                provider == SocialProvider.NAVER ? null : LocalDateTime.now().plusSeconds(socialAuthToken.getRefreshTokenExpiresIn())
        );
    }

    public void updateAuthProfile(String email, OAuthTokenResponse response) {
        this.email = email;
        this.socialAuthToken.updateToken(response);
    }

    public void updateAuthToken(SocialAuthToken socialAuthToken) {
        this.socialAuthToken = socialAuthToken;
    }

    public void connectBrand(String brand){
        this.brand = brand;
    }

    public void connectAddress(String address) {
        this.address = address;
    }

    public void changeTags(Set<FavoriteTag> tags) {
        this.tags.clear();
        tags.forEach(tag -> {
            tag.connectMember(this);
            tag.increaseScore(TagType.REGISTER);
        });
        this.tags.addAll(tags);

    }

    public void addTag(FavoriteTag tag){
        getTags().add(tag);
        tag.connectMember(this);
    }
}
