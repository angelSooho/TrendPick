package project.trendpick_pro.domain.product.entity.product.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.trendpick_pro.domain.product.entity.product.Product;

import java.io.Serializable;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductListResponse implements Serializable {

    private Long id;
    private String name;
    private String brand;
    private String mainFile;
    private int price;
    private double discountRate;
    private int discountedPrice;

    @QueryProjection
    public ProductListResponse(Long id, String name, String brand, String mainFile, int price, double discountRate) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.mainFile = mainFile;
        this.price = price;
        this.discountRate = (int) discountRate;
    }

    @Builder
    public ProductListResponse(Long id, String name, String brand, String mainFile, int price, double discountRate, int discountedPrice) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.mainFile = mainFile;
        this.price = price;
        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
    }

    public static ProductListResponse of(Product product) {
        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getTitle())
                .brand(product.getProductOption().getBrand().getName())
                .mainFile(product.getProductOption().getFile().getFileName())
                .price(product.getProductOption().getPrice())
                .discountRate(product.getDiscountRate())
                .discountedPrice(product.getProductOption().getPrice() - (int) (product.getProductOption().getPrice() * (product.getDiscountRate() / 100)))
                .build();
    }
}