package project.trendpick_pro.domain.member.entity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.trendpick_pro.domain.member.entity.Member;

@Getter
@NoArgsConstructor
public class MemberInfoResponse {

    private String email;
    private String nickName;
    private String address;
    private String phone;

    @Builder
    private MemberInfoResponse(Member member) {
        this.nickName = member.getNickName();
        this.email = member.getEmail();
        this.phone = member.getPhoneNumber();
        this.address = member.getAddress();
    }

    public static MemberInfoResponse of(Member member){
        return new MemberInfoResponse(member);
    }
}
