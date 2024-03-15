package project.trendpick_pro.domain.product.entity.product.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductListResponse {

    private Long id;
    private String name;
    private String brand;
    private String mainFile;
    private int price;
    private double discountRate;

    @Builder
    @QueryProjection
    public ProductListResponse(Long id, String name, String brand, String mainFile, int price, double discountRate) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.mainFile = mainFile;
        this.price = price;
        this.discountRate = discountRate;
    }
}
