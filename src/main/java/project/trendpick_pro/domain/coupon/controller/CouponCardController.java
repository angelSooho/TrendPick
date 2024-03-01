package project.trendpick_pro.domain.coupon.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.coupon.entity.dto.response.CouponCardByApplyResponse;
import project.trendpick_pro.domain.coupon.service.CouponCardService;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/couponcards")
public class CouponCardController {

    private final CouponCardService couponCardService;

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<Void> issueCoupon(
            @MemberEmail String email,
            @PathVariable("couponId") Long couponId) {
        couponCardService.issue(email, couponId, LocalDateTime.now());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping
    public ResponseEntity<List<CouponCardByApplyResponse>> get(@RequestParam("orderItem") Long orderItemId) {
        return ResponseEntity.ok().body(couponCardService.getCouponsByOrdered(orderItemId));
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/apply")
    public ResponseEntity<Void> apply(HttpServletRequest req, @RequestParam("couponCard") Long couponCardId, @RequestParam("orderItem") Long orderItemId) {
        couponCardService.apply(couponCardId, orderItemId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelCoupon(@RequestParam("orderItem") Long orderItemId, HttpServletRequest req) {
        couponCardService.cancel(orderItemId);
        return ResponseEntity.ok().build();
    }
}
