package project.trendpick_pro.domain.coupon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.coupon.entity.dto.request.CouponSaveRequest;
import project.trendpick_pro.domain.coupon.entity.dto.response.CouponResponse;
import project.trendpick_pro.domain.coupon.service.CouponService;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PreAuthorize("hasAuthority({'BRAND_ADMIN'})")
    @PostMapping("/{storeName}/generate")
    public ResponseEntity<Void> createCoupon(@PathVariable("storeName") String storeName, @Valid CouponSaveRequest request) {
        couponService.saveCoupons(storeName, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/list")
    public ResponseEntity<List<CouponResponse>> getCoupons(){
        return ResponseEntity.ok().body(couponService.getCoupons());
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/box")
    public ResponseEntity<List<CouponResponse>> getCouponsByProduct(@RequestParam("productId") Long productId){
        return ResponseEntity.ok().body(couponService.findCouponsByProduct(productId));
    }
}
