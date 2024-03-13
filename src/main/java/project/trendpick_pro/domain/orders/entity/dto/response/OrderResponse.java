package project.trendpick_pro.domain.orders.entity.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.trendpick_pro.domain.orders.entity.Order;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderResponse {
    private String productFilePath;
    private String brandName;
    private String productName;
    private int count;
    private LocalDateTime orderDate;
    private LocalDateTime canceledDate;
    private String size;
    private String color;
    private int productPrice;
    private int discountPrice;
    private String orderStatus;
    private String deliveryStatus;

    @Builder
    @QueryProjection
    public OrderResponse(String productFilePath, String brandName, String productName, int count,int productPrice, int discountPrice, LocalDateTime orderDate, LocalDateTime canceledDate,
                         String size, String color, String orderStatus, String deliveryStatus) {
        this.productFilePath = productFilePath;
        this.brandName = brandName;
        this.productName = productName;
        this.count = count;
        this.productPrice = productPrice;
        this.discountPrice = discountPrice;
        this.orderDate = orderDate;
        this.canceledDate = canceledDate;
        this.size = size;
        this.color = color;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
    }

    public static OrderResponse of(Order order) {
        return OrderResponse.builder()
                .productFilePath(order.getOrderItems().get(0).getProduct().getProductOption().getFile().getFileName())
                .brandName(order.getMember().getBrand())
                .productName(order.getOrderItems().get(0).getProduct().getTitle())
                .count(order.getOrderItems().get(0).getQuantity())
                .productPrice(order.getOrderItems().get(0).getOrderPrice())
                .discountPrice(order.getOrderItems().get(0).getDiscountPrice())
                .orderDate(order.getCreatedDate())
                .canceledDate(LocalDateTime.now())
                .size(order.getOrderItems().get(0).getSize())
                .color(order.getOrderItems().get(0).getColor())
                .orderStatus(order.getOrderStatus().name())
                .deliveryStatus(order.getDelivery().getAddress())
                .build();
    }

    public int getTotalPrice(){
        return productPrice * count - discountPrice;
    }
}
