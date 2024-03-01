package project.trendpick_pro.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.coupon.entity.Coupon;
import project.trendpick_pro.domain.coupon.entity.dto.request.CouponSaveRequest;
import project.trendpick_pro.domain.coupon.entity.dto.response.CouponResponse;
import project.trendpick_pro.domain.coupon.entity.expirationPeriod.ExpirationType;
import project.trendpick_pro.domain.coupon.repository.CouponRepository;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.store.entity.Store;
import project.trendpick_pro.domain.store.service.StoreService;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final StoreService storeService;
    private final ProductService productService;

    @Transactional
    public void saveCoupons(String storeName, CouponSaveRequest request) {
        validateExpirationPeriod(request);
        Store store = storeService.findByBrand(storeName);
        settingCoupon(request, store);
    }

    public List<CouponResponse> getCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::of)
                .toList();
    }

    public List<CouponResponse> findCouponsByProduct(Long productId) {
        Product product = productService.findByIdWithBrand(productId);
        List<Coupon> coupons = filteredCoupons(product);
        return coupons.stream()
                .map(CouponResponse::of)
                .toList();
    }

    private void settingCoupon(CouponSaveRequest request, Store store) {
        Coupon coupon = Coupon.of(request, store.getBrand());
        updateExpirationType(request, coupon);
        coupon.connectStore(store);
        couponRepository.save(coupon);
    }

    private static void updateExpirationType(CouponSaveRequest request, Coupon coupon) {
        if(request.getExpirationType().equals(ExpirationType.PERIOD.getValue()))
            coupon.assignPeriodExpiration(request.getStartDate(), request.getEndDate());
        else if(request.getExpirationType().equals(ExpirationType.ISSUE_AFTER_DATE.getValue()))
            coupon.assignPostIssueExpiration(request.getIssueAfterDate());
    }

    private List<Coupon> filteredCoupons(Product product) {
        List<Coupon> coupons = couponRepository.findAllByBrand(product.getProductOption().getBrand().getName());
        return coupons.stream()
                .filter(coupon -> coupon.validateMinimumPurchaseAmount(product.getProductOption().getPrice())
                        && coupon.validateLimitCount()
                        && coupon.validateLimitIssueDate(LocalDateTime.now()))
                .toList();
    }

    private void validateExpirationPeriod(CouponSaveRequest request) {
        if (request.getExpirationType().equals(ExpirationType.ISSUE_AFTER_DATE.getValue())
         && request.getIssueAfterDate() == null) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "유효기간 타입이 발급 후 날짜 선택일 경우 날짜를 선택해주세요.");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "유효기간 타입이 기간 선택일 경우 시작일과 종료일을 선택해주세요.");
        }
        if (request.getStartDate().isBefore(LocalDateTime.now().plusDays(1).with(LocalTime.MIDNIGHT))) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "유효기간 시작일은 오늘 이후여야 합니다.");
        }
        if (request.getEndDate().isBefore(request.getStartDate().plusDays(1))) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "유효기간 종료일은 시작일 이후여야 합니다.");
        }
    }
}
