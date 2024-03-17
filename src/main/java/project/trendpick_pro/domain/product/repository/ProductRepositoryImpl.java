package project.trendpick_pro.domain.product.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import project.trendpick_pro.domain.product.entity.product.dto.request.ProductSearchCond;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductByRecommended;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponse;
import project.trendpick_pro.domain.product.entity.product.dto.response.QProductByRecommended;
import project.trendpick_pro.domain.product.entity.product.dto.response.QProductListResponse;

import java.util.List;

import static project.trendpick_pro.domain.brand.entity.QBrand.brand;
import static project.trendpick_pro.domain.category.entity.QMainCategory.mainCategory;
import static project.trendpick_pro.domain.category.entity.QSubCategory.subCategory;
import static project.trendpick_pro.domain.common.file.QCommonFile.commonFile;
import static project.trendpick_pro.domain.product.entity.product.QProduct.product;
import static project.trendpick_pro.domain.product.entity.productOption.QProductOption.productOption;
import static project.trendpick_pro.domain.tags.favoritetag.entity.QFavoriteTag.favoriteTag;
import static project.trendpick_pro.domain.tags.tag.entity.QTag.tag;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public ProductRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
        this.em = em;
    }

    @Override
    public Page<ProductListResponse> findAllByCategoryId(ProductSearchCond cond, Pageable pageable) {
        List<ProductListResponse> result = queryFactory
                .select(new QProductListResponse(
                        product.id,
                        product.title,
                        brand.name,
                        commonFile.fileName,
                        productOption.price,
                        product.discountRate
                ))
                .from(product)
                .leftJoin(product.productOption, productOption)
                .leftJoin(productOption.mainCategory, mainCategory)
                .leftJoin(productOption.subCategory, subCategory)
                .leftJoin(productOption.brand, brand)
                .leftJoin(productOption.file, commonFile)
                .where(
                        mainCategoryEq(cond),
                        subCategoryEq(cond)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(product.productOption, productOption)
                .leftJoin(productOption.mainCategory, mainCategory)
                .leftJoin(productOption.subCategory, subCategory)
                .leftJoin(productOption.brand, brand)
                .leftJoin(productOption.file, commonFile)
                .where(
                        mainCategoryEq(cond),
                        subCategoryEq(cond)
                );

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }

    @Override
    public List<ProductByRecommended> findRecommendProduct(String email) {
        return queryFactory
                .select(new QProductByRecommended(
                        tag.product.id,
                        tag.name
                ))
                .from(tag)
                .where(tag.name.in(
                    JPAExpressions.select(favoriteTag.name)
                        .from(favoriteTag)
                        .where(favoriteTag.member.email.eq(email))
                    )
                )
                .distinct()
                .fetch();
    }

    @Override
    public Page<ProductListResponse> findAllByKeyword(ProductSearchCond cond, Pageable pageable) {

        String sql = "SELECT p.id, p.title, b.name, f.file_name, po.price, p.discount_rate " +
                "FROM product p " +
                "LEFT JOIN product_option po ON p.product_option_id = po.id " +
                "LEFT JOIN main_category mc ON po.main_category_id = mc.id " +
                "LEFT JOIN sub_category sc ON po.sub_category_id = sc.id " +
                "LEFT JOIN brand b ON po.brand_id = b.id " +
                "LEFT JOIN common_file f ON po.common_file_id = f.id " +
                "WHERE MATCH(p.title) AGAINST(:keyword IN BOOLEAN MODE) " +
                "OR MATCH(b.name) AGAINST(:keyword IN BOOLEAN MODE) " +
                "OR MATCH(mc.name) AGAINST(:keyword IN BOOLEAN MODE) " +
                "OR MATCH(sc.name) AGAINST(:keyword IN BOOLEAN MODE) " +
                "LIMIT :offset, :limit";

        Query query = em.createNativeQuery(sql);
        query.setParameter("keyword", cond.getKeyword());
        query.setParameter("offset", pageable.getOffset());
        query.setParameter("limit", pageable.getPageSize());
        List<Object> objects = query.getResultList();
        List<ProductListResponse> result = objects.stream()
                .parallel().map(
                row -> new ProductListResponse(
                        (Long) ((Object[]) row)[0],
                        (String) ((Object[]) row)[1],
                        (String) ((Object[]) row)[2],
                        (String) ((Object[]) row)[3],
                        (Integer) ((Object[]) row)[4],
                        (Double) ((Object[]) row)[5]
                )).toList();

        sql = "SELECT COUNT(*) " +
                "FROM product p " +
                "LEFT JOIN product_option po ON p.product_option_id = po.id " +
                "LEFT JOIN main_category mc ON po.main_category_id = mc.id " +
                "LEFT JOIN sub_category sc ON po.sub_category_id = sc.id " +
                "LEFT JOIN brand b ON po.brand_id = b.id " +
                "LEFT JOIN common_file f ON po.common_file_id = f.id " +
                "WHERE MATCH(p.title) AGAINST(:keyword IN BOOLEAN MODE) " +
                "OR MATCH(b.name) AGAINST(:keyword IN BOOLEAN MODE) " +
                "OR MATCH(mc.name) AGAINST(:keyword IN BOOLEAN MODE) " +
                "OR MATCH(sc.name) AGAINST(:keyword IN BOOLEAN MODE)";

        query = em.createNativeQuery(sql);
        query.setParameter("keyword", cond.getKeyword());
        int count = query.getFirstResult();

        return PageableExecutionUtils.getPage(result, pageable, () -> count);
    }

    private static BooleanExpression mainCategoryEq(ProductSearchCond cond) {
        if (cond.getMainCategory().equals("전체")) {
            return null;
        } else {
            return mainCategory.name.eq(cond.getMainCategory());
        }
    }

    private static BooleanExpression subCategoryEq(ProductSearchCond cond) {
        if (cond.getSubCategory().equals("전체")) {
            return null;
        } else {
            return subCategory.name.eq(cond.getSubCategory());
        }
    }
}
