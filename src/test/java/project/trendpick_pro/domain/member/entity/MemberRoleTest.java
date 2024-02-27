package project.trendpick_pro.domain.member.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRoleTest {

    private static Stream<Arguments> CheckingMemberType() {
        return Stream.of(
                Arguments.of("ROLE_MEMBER", true),
                Arguments.of("ROLE_ADMIN", true),
                Arguments.of("ROLE_BRAND_ADMIN", true)
        );
    }

    @DisplayName("멤버 타입이 정의 되어있는 타입인지 체크한다.")
    @MethodSource("CheckingMemberType")
    @ParameterizedTest
    void containsMemberType(String memberRole, boolean expected) throws Exception {
        //when
        MemberRole result = MemberRole.isType(memberRole);

        //then
        assertThat(result).isNotNull()
                .isEqualTo(MemberRole.valueOf(memberRole));
    }
}