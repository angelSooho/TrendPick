package project.trendpick_pro.domain.product.entity.product.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.productOption.Color;
import project.trendpick_pro.domain.product.entity.productOption.Size;
import project.trendpick_pro.domain.tags.tag.entity.Tag;
import project.trendpick_pro.global.config.AmazonProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductResponse {

    private String productCode;
    private String name;
    private String mainCategory;
    private String subCategory;
    private String brand;
    private String description;
    private String mainFile;
    private List<String> sizes;
    private List<String> colors;
    private int price;
    private List<String> tags = new ArrayList<>();
    private int discountRate;
    private int discountedPrice;

    @Builder
    @QueryProjection
    public ProductResponse(String productCode, String name, String mainCategory, String subCategory, String brand, String description,
                           String mainFile, List<String> sizes, List<String> colors, int price, List<String> tags,
                           double discountRate) {
        this.productCode = productCode;
        this.name = name;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.brand = brand;
        this.description = description;
        this.mainFile = mainFile;
        this.sizes = sizes;
        this.colors = colors;
        this.price = price;
        this.tags = tags;
        this.discountRate = (int) discountRate;
    }

    public ProductResponse(String productCode, String name, String mainCategory, String subCategory, String brand, String description,
                           String mainFile, List<String> sizes, List<String> colors, int price, List<String> tags,
                           double discountRate, int discountedPrice) {
        this.productCode = productCode;
        this.name = name;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.brand = brand;
        this.description = description;
        this.mainFile = mainFile;
        this.sizes = sizes;
        this.colors = colors;
        this.price = price;
        this.tags = tags;
        this.discountRate = (int) discountRate;
        this.discountedPrice = discountedPrice;
    }

    public static ProductResponse of(Product product, AmazonProperties amazonProperties) {
        return new ProductResponse(
                product.getProductCode(),
                product.getTitle(),
                product.getProductOption().getMainCategory().getName(),
                product.getProductOption().getSubCategory().getName(),
                product.getProductOption().getBrand().getName(),
                product.getDescription(),
                amazonProperties.getEndpoint() + "/" + product.getProductOption().getFile().getFileName(),
                product.getProductOption().getSizes().stream().map(Size::getName).toList(),
                product.getProductOption().getColors().stream().map(Color::getName).toList(),
                product.getProductOption().getPrice(),
                product.getTags().stream().map(Tag::getName).toList() ,
                product.getDiscountRate(),
                product.getProductOption().getPrice() - (int) (product.getProductOption().getPrice() * (product.getDiscountRate() / 100.0))
        );
    }

    private static List<String> subFiles(List<CommonFile> subFiles) {
        List<String> tmpList = new ArrayList<>();

        for (CommonFile subFile : subFiles) {
            tmpList.add(subFile.getFileName());
        }
        return tmpList;
    }
}