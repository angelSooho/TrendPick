package project.trendpick_pro.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.coupon.entity.Coupon;
import project.trendpick_pro.domain.coupon.entity.CouponCard;
import project.trendpick_pro.domain.coupon.entity.dto.response.CouponCardByApplyResponse;
import project.trendpick_pro.domain.coupon.exception.CouponNotFoundException;
import project.trendpick_pro.domain.coupon.repository.CouponCardRepository;
import project.trendpick_pro.domain.coupon.repository.CouponRepository;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.entity.OrderItem;
import project.trendpick_pro.domain.orders.exception.OrderItemNotFoundException;
import project.trendpick_pro.domain.orders.repository.OrderItemRepository;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponCardService {

    private final CouponCardRepository couponCardRepository;
    private final CouponRepository couponRepository;
    private final OrderItemRepository orderItemRepository;

    private final MemberService memberService;

    @Transactional
    public void issue(String email, Long couponId, LocalDateTime dateTime) {
        Member member = memberService.findByEmail(email);
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(
                () -> new CouponNotFoundException("존재하지 않는 쿠폰입니다."));
        int count = couponCardRepository.countByCouponIdAndMemberId(couponId, member.getId());

        validateCouponCard(count, coupon);
        settingCouponCard(member, dateTime, coupon);
    }

    @Transactional
    public void apply(Long couponCardId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException("주문되지 않은 상품입니다."));
        CouponCard couponCard = couponCardRepository.findById(couponCardId).orElseThrow(
                () -> new CouponNotFoundException("존재하지 않은 쿠폰입니다."));
        if (!couponCard.validate(orderItem, LocalDateTime.now()))
            throw new BaseException(ErrorCode.BAD_REQUEST, "쿠폰이 적용되지 않습니다.");
        couponCard.use(orderItem, LocalDateTime.now());
    }

    @Transactional
    public void cancel(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException("주문되지 않은 상품입니다."));
        orderItem.getCouponCard().cancel(orderItem);
    }

    public List<CouponCardByApplyResponse> getCouponsByOrdered(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException("주문되지 않은 상품입니다."));
        List<CouponCard> couponCards = couponCardRepository.findAllByBrand(orderItem.getProduct().getProductOption().getBrand().getName());
        return createCouponCardByApplyResponseList(couponCards, orderItem);
    }

    private void settingCouponCard(Member member, LocalDateTime dateTime, Coupon coupon) {
        CouponCard couponCard = new CouponCard(coupon);
        couponCard.updatePeriod(dateTime);
        couponCard.updatePeriod(dateTime);
        couponCard.connectMember(member);
        couponCardRepository.save(couponCard);
    }

    private static void validateCouponCard(int count, Coupon coupon) {
        if(count > 0)
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 발급된 쿠폰입니다.");
        if(!coupon.validateLimitCount())
            throw new BaseException(ErrorCode.BAD_REQUEST, "쿠폰 발급 가능 횟수를 초과하였습니다.");
        if(!coupon.validateLimitIssueDate(LocalDateTime.now()))
            throw new BaseException(ErrorCode.BAD_REQUEST, "쿠폰 발급 기간이 만료되었습니다.");
    }

    private List<CouponCardByApplyResponse> createCouponCardByApplyResponseList(List<CouponCard> couponCards, OrderItem orderItem) {
        return couponCards.stream()
                .filter(couponCard -> couponCard.validate(orderItem, LocalDateTime.now()))
                .map(couponCard -> CouponCardByApplyResponse.of(couponCard, orderItem))
                .toList();
    }
}
