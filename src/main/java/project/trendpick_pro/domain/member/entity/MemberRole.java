package project.trendpick_pro.domain.member.entity;

import lombok.Getter;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.stream.Stream;

@Getter
public enum MemberRole {
    ADMIN("ROLE_ADMIN"),
    BRAND_ADMIN("ROLE_BRAND_ADMIN"),
    MEMBER("ROLE_MEMBER");

    private String value;

    MemberRole(String value) {
        this.value = value;
    }

    public static MemberRole isType(String value) {
        return Stream.of(MemberRole.values())
                .filter(v -> v.getValue().equals(value))
                .findFirst().orElseThrow(() -> new BaseException(ErrorCode.NOT_MATCH));
    }
}
