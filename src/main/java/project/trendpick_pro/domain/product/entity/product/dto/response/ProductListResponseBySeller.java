package project.trendpick_pro.domain.product.entity.product.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductListResponseBySeller {

    private Long id;
    private String name;
    private String mainFile;
    private int price;
    private int stock;
    private LocalDateTime createdDate;

    private double rateAvg;
    private int reviewCount;

    private int discountRate;
    private int discountedPrice;

    @Builder
    @QueryProjection
    public ProductListResponseBySeller(Long id, String name, String mainFile, int price, int stock, LocalDateTime createdDate,
                                       double rateAvg, int reviewCount, double discountRate, int discountedPrice) {
        this.id = id;
        this.name = name;
        this.mainFile = mainFile;
        this.price = price;
        this.stock = stock;
        this.createdDate = createdDate;
        this.rateAvg = rateAvg;
        this.reviewCount = reviewCount;
        this.discountRate = (int) discountRate;
        this.discountedPrice = discountedPrice;
    }

    public String getFormattedPrice(){
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        return numberFormat.format(getPrice())+"원";
    }
}
