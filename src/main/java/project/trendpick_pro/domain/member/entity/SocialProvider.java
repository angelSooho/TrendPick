package project.trendpick_pro.domain.member.entity;

import lombok.Getter;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

@Getter
public enum SocialProvider {
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");

    private String value;

    SocialProvider(String value){
        this.value = value;
    }

    public static SocialProvider isType(String provider){
        for(SocialProvider type : values()){
            if(type.value.equals(provider)){
                return type;
            }
        }
        throw new BaseException(ErrorCode.BAD_REQUEST, "잘못된 소셜 로그인 타입입니다.");
    }

}
