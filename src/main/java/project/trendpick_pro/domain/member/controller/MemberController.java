package project.trendpick_pro.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.member.entity.dto.MemberInfoResponse;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.global.basedata.tagname.service.TagNameService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final TagNameService tagNameService;

    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @NotNull @RequestParam("code") String code,
            @NotNull @RequestParam("provider") String provider,
            @NotNull @RequestParam("state") String state,
            HttpServletResponse response) {
        memberService.login(code, provider, state, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reissue")
    public ResponseEntity<Void> reissue(
            HttpServletRequest request,
            HttpServletResponse response,
            @MemberEmail String email) {
        memberService.reissueToken(request, response, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                         @MemberEmail String email) {
        memberService.logoutMember(request, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/revoke")
    public ResponseEntity<Void> revoke(HttpServletRequest request,
                         @MemberEmail String email) {
        memberService.revokeMember(request, email);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponse> myInfo(@MemberEmail String email) {
        return ResponseEntity.ok(memberService.getMemberInfo(email));
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @PostMapping("/edit/address")
    public ResponseEntity<MemberInfoResponse> modifyAddress(@NotNull @RequestBody String address,
                                                            @MemberEmail String email) {
        return ResponseEntity.ok(memberService.modifyAddress(address, email));
    }
}
