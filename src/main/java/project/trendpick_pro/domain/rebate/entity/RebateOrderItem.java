package project.trendpick_pro.domain.rebate.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import project.trendpick_pro.domain.brand.entity.Brand;
import project.trendpick_pro.domain.cash.entity.CashLog;
import project.trendpick_pro.domain.common.base.BaseTimeEntity;
import project.trendpick_pro.domain.coupon.entity.CouponCard;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.orders.entity.Order;
import project.trendpick_pro.domain.orders.entity.OrderItem;
import project.trendpick_pro.domain.product.entity.product.Product;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RebateOrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rebate_order_item_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "coupon_card_id")
    private CouponCard couponCard;
    @Column(name = "total_price")
    private int totalPrice; //전체금액 (할인제외)
    @Column(name = "order_price", nullable = false)
    private int orderPrice; //주문금액 (할인 계산된 금액)
    @Column(name = "discount_price")
    private int discountPrice; //할인 받은 금액

    // 상품
    @Column(name = "product_subject", nullable = false)
    private String productSubject;
    @Column(name = "size", nullable = false)
    private String size;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "count", nullable = false)
    private int quantity;
    @OneToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CashLog rebateCashLog; // 정산에 관련된 환급지급내역
    private LocalDateTime rebateDate;

    // 주문품목
    private LocalDateTime orderItemCreateDate;

    // 구매자 회원
    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member buyer;
    private String buyerName;

    // 판매자 회원
    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Brand seller;
    private String sellerName;

    public RebateOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
        order=orderItem.getOrder();
        product=orderItem.getProduct();
        couponCard=orderItem.getCouponCard();
        orderPrice=orderItem.getOrderPrice();
        totalPrice=orderItem.getOrderItemByQuantity();
        discountPrice=orderItem.getDiscountPrice();
        size=orderItem.getSize();
        color=orderItem.getColor();
        quantity=orderItem.getQuantity();
        // 상품 추가 데이터
        productSubject=orderItem.getProduct().getTitle();

        // 주문 품목 추가데이터
        orderItemCreateDate=orderItem.getOrder().getCreatedDate();

        // 구매자 추가 데이터
        buyer=orderItem.getOrder().getMember();
        buyerName=orderItem.getOrder().getMember().getNickName();

        // 판매자 추가 데이터
        seller=orderItem.getProduct().getProductOption().getBrand();
        sellerName=orderItem.getProduct().getProductOption().getBrand().getName();
    }

    public int calculateRebatePrice() {
        return (totalPrice*quantity - discountPrice) - (int)(totalPrice * 0.05); // 정산금액 수수료 5%(임의) 제외하고 계산
    }
    public boolean checkAlreadyRebate() {
        return rebateDate == null;
    }

    public void setRebateDone(CashLog cashLog) {
        rebateDate = LocalDateTime.now();
        this.rebateCashLog = cashLog;
    }

    public boolean isRebateDone() {
        return rebateDate != null;
    }

    public void updateWith(RebateOrderItem item){
        orderItem = item.getOrderItem();
        order=item.getOrder();
        product=item.getProduct();
        couponCard=item.getCouponCard();
        orderPrice=item.getOrderPrice();
        totalPrice=item.getTotalPrice();
        discountPrice=item.getDiscountPrice();
        size=item.getSize();
        color=item.getColor();
        quantity=item.getQuantity();
        productSubject=item.getProduct().getTitle();
        orderItemCreateDate=item.getOrder().getCreatedDate();
        buyer=item.getOrder().getMember();
        buyerName=item.getOrder().getMember().getNickName();
        seller=item.getProduct().getProductOption().getBrand();
        sellerName=item.getProduct().getProductOption().getBrand().getName();
    }
}
