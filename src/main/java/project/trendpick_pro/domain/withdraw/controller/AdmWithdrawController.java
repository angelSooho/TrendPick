package project.trendpick_pro.domain.withdraw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.withdraw.entity.WithdrawApply;
import project.trendpick_pro.domain.withdraw.service.WithdrawService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdmWithdrawController {

    private final WithdrawService withdrawService;

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @GetMapping("/withDrawList")
    public ResponseEntity<List<WithdrawApply>> showApplyList(@MemberEmail String email) {
        return ResponseEntity.ok().body(withdrawService.getWithdraws(email));
    }

//    @PreAuthorize("hasAuthority({'ADMIN'})")
//    @PostMapping("/{withdrawApplyId}")
//    public ResponseEntity<Void> applyDone(@PathVariable Long withdrawApplyId) {
//        withdrawService.withdraw(withdrawApplyId);
//        return ResponseEntity.noContent().build();
//    }

    @PreAuthorize("hasAuthority({'ADMIN'})")
    @PostMapping("/{withdrawApplyId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long withdrawApplyId) {
        withdrawService.cancelApply(withdrawApplyId);
        return ResponseEntity.noContent().build();
    }
}
