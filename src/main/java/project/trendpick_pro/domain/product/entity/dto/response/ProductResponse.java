package project.trendpick_pro.domain.product.entity.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.product.entity.Product;
import project.trendpick_pro.domain.tags.tag.entity.Tag;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductResponse {

    private Long id;
    private String name;
    private String mainCategory;
    private String subCategory;
    private String brand;
    private String description;
    private String mainFile;
    private List<String> subFiles;
    private int price;
    private int stock;
    private List<Tag> tags = new ArrayList<>();

    private int discountRate;

    private int discountedPrice;

    @Builder
    @QueryProjection
    public ProductResponse(Long id, String name, String mainCategory, String subCategory, String brand, String description,
                           String mainFile, List<String> subFiles, int price, int stock, List<Tag> tags, double discountRate, int discountedPrice) {
        this.id = id;
        this.name = name;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.brand = brand;
        this.description = description;
        this.mainFile = mainFile;
        this.subFiles = subFiles;
        this.price = price;
        this.stock = stock;
        this.tags = tags;
        this.discountRate = (int) discountRate;
        this.discountedPrice = discountedPrice;
    }

    public static ProductResponse of (Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .mainCategory(product.getMainCategory().getName())
                .subCategory(product.getSubCategory().getName())
                .brand(product.getBrand().getName())
                .description(product.getDescription())
                .mainFile(product.getFile().getFileName())
                .subFiles(subFiles(product.getFile().getChild()))
                .price(product.getPrice())
                .stock(product.getStock())
                .tags(new ArrayList<>(product.getTags()))
                .discountedPrice(product.getDiscountedPrice())
                .discountRate(product.getDiscountRate())
                .build();
    }

    private static List<String> subFiles(List<CommonFile> subFiles) {
        List<String> tmpList = new ArrayList<>();

        for (CommonFile subFile : subFiles) {
            tmpList.add(subFile.getFileName());
        }
        return tmpList;
    }
}