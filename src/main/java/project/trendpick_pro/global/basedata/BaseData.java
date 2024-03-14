package project.trendpick_pro.global.basedata;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import project.trendpick_pro.domain.brand.entity.Brand;
import project.trendpick_pro.domain.category.entity.MainCategory;
import project.trendpick_pro.domain.category.entity.SubCategory;
import project.trendpick_pro.domain.category.service.MainCategoryService;
import project.trendpick_pro.domain.category.service.SubCategoryService;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRole;
import project.trendpick_pro.domain.member.entity.SocialProvider;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.ProductStatus;
import project.trendpick_pro.domain.product.entity.productOption.ProductOption;
import project.trendpick_pro.domain.tags.tag.entity.Tag;
import project.trendpick_pro.global.basedata.tagname.entity.TagName;
import project.trendpick_pro.global.config.AmazonProperties;
import project.trendpick_pro.global.config.DataProperties;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile({"local"})
@RequiredArgsConstructor
public class BaseData implements
        ApplicationListener<ContextRefreshedEvent> {

    private final AmazonProperties amazonProperties;
    private final DataProperties dataProperties;
    private final AmazonS3Client amazonS3Client;

    private final JdbcTemplate jdbcTemplate;

    private final MainCategoryService mainCategoryService;
    private final SubCategoryService subCategoryService;

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        long startTime = System.currentTimeMillis();
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("상의", dataProperties.getTop());
        categories.put("하의", dataProperties.getBottom());
        categories.put("신발", dataProperties.getShoes());
        categories.put("아우터", dataProperties.getOuter());
        categories.put("가방", dataProperties.getBag());
        categories.put("액세서리", dataProperties.getAccessory());

        int memberCount = 100;
        int fileCount = 100;
        int productCount = 10_000_000;

        executionTime("member insert", () -> saveMembersBulk(memberCount));
        executionTime("brandMember insert", () -> makeBrandMembersBulk(dataProperties.getBrand()));
        executionTime("mainCategory insert", () -> saveMainCategoriesBulk(dataProperties.getMainCategory()));
        executionTime("subCategory insert", () -> saveSubCategoriesBulk(categories));
        executionTime("file insert", () -> saveFilesBulk(fileCount));
            executionTime("productOption insert", () -> {
                try {
                    saveProductOptionsBulk(productCount, fileCount);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
        executionTime("product insert", () -> {
            try {
                saveProductsBulk(productCount);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void executionTime(String taskName, Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        double resultTime = (endTime - startTime) / 1000.0;
        if (resultTime > 60) {
            log.info("{}: {} min {} sec", taskName, (int) (resultTime / 60), (int) (resultTime % 60));
        } else {
            log.info("{}: {} sec", taskName, resultTime);
        }
    }

    public void saveMembersBulk(int count) {
        List<Member> memberList = new ArrayList<>();
        for(int i=1; i<=count; i++){
            Member member = Member.builder()
                    .email("member" + i + "@naver.com")
                    .nickName("member" + i)
                    .phoneNumber("010-1234-1234")
                    .provider(SocialProvider.NAVER)
                    .role(MemberRole.MEMBER)
                    .build();
            memberList.add(member);
            member.connectAddress("서울특별시 어디구 어디로 123");
        }
        Member admin = Member.builder()
                .email("admin@test.com")
                .nickName("admin")
                .phoneNumber("010-1234-1234")
                .provider(SocialProvider.NAVER)
                .role(MemberRole.ADMIN)
                .build();
        admin.connectAddress("서울특별시 어디구 어디로 123");
        memberList.add(admin);

        String sql = "INSERT INTO member (email, nick_name, phone_number, provider, role, address) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Member member = memberList.get(i);
                ps.setString(1, member.getEmail());
                ps.setString(2, member.getNickName());
                ps.setString(3, member.getPhoneNumber());
                ps.setString(4, member.getProvider().getValue());
                ps.setString(5, member.getRole().getValue());
                ps.setString(6, member.getAddress());
            }

            @Override
            public int getBatchSize() {
                return memberList.size();
            }
        });
    }

    private void makeBrandMembersBulk(List<String> brandNames) {
        List<Member> members = new ArrayList<>();
        for (String brandName : brandNames) {
            Member member = Member.builder()
                    .email(brandName + "@naver.com")
                    .nickName(brandName)
                    .phoneNumber("010-1234-1234")
                    .provider(SocialProvider.NAVER)
                    .role(MemberRole.BRAND_ADMIN)
                    .brand(brandName)
                    .build();
            members.add(member);
        }

        String sql = "INSERT INTO member (email, nick_name, phone_number, provider, role, brand) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Member member = members.get(i);
                ps.setString(1, member.getEmail());
                ps.setString(2, member.getNickName());
                ps.setString(3, member.getPhoneNumber());
                ps.setString(4, member.getProvider().getValue());
                ps.setString(5, member.getRole().getValue());
                ps.setString(6, member.getBrand());
            }

            @Override
            public int getBatchSize() {
                return members.size();
            }
        });
    }

    public void saveMainCategoriesBulk(List<String> mainCategoryNames) {
        List<MainCategory> mainCategories = new ArrayList<>();
        for (String name : mainCategoryNames) {
            MainCategory mainCategory = new MainCategory(name);
            mainCategories.add(mainCategory);
        }
        String sql = "INSERT INTO main_category (name) VALUES (?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MainCategory mainCategory = mainCategories.get(i);
                ps.setString(1, mainCategory.getName());
            }

            @Override
            public int getBatchSize() {
                return mainCategories.size();
            }
        });
    }

    public void saveSubCategoriesBulk(Map<String, List<String>> categories) {
        String sql = "INSERT INTO sub_category (name, category_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, categories.get("상의").get(i));
                ps.setLong(2, RandomUtils.nextLong(1, 5));
            }

            @Override
            public int getBatchSize() {
                return categories.get("상의").size();
            }
        });
    }

    public void saveProductsBulk(int count) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Product> products = new CopyOnWriteArrayList<>();
        for (int n = 1; n <= count; n++) {
            int currentN = n;
            executor.execute(() -> {
                Product product = Product
                        .builder()
                        .productCode("P" + UUID.randomUUID())
                        .title(currentN + " title 멋사입니다. ")
                        .description(currentN + " description 멋사입니다. ")
                        .build();
                Set<Tag> tags = new LinkedHashSet<>();
                for (int i = 0; i <= 4; i++) {
                    Tag tag = new Tag(new TagName("tag" + i).getName());
                    tags.add(tag);
                }
                product.updateTags(tags);
                products.add(product);
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(1L, TimeUnit.HOURS)) {
            executor.shutdownNow();
        }

        String sql = "INSERT INTO product (product_code, title, description, product_option_id, review_count, rate_avg, discount_rate, discounted_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product product = products.get(i);
                ps.setString(1, product.getProductCode());
                ps.setString(2, product.getTitle());
                ps.setString(3, product.getDescription());
                ps.setLong(4, 5);
                ps.setInt(5, 23);
                ps.setDouble(6, 1);
                ps.setDouble(7, 1);
                ps.setInt(8, 29000);
            }

            @Override
            public int getBatchSize() {
                return products.size();
            }
        });
    }

    public void saveProductOptionsBulk(int count, int fileCount) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<ProductOption> productOptions = new CopyOnWriteArrayList<>();
        MainCategory mainCategory = mainCategoryService.findByName(dataProperties.getMainCategory().get((int) (Math.random() * 5)));
        SubCategory subCategory = subCategoryService.findByName(dataProperties.getBottom().get((int) (Math.random() * 5)));
        for (int i = 0; i < count; i++) {
            executor.submit(() -> {
                int stockRandom = 123;
                int priceRandom = 25000;

                List<String> colors = dataProperties.getColors();
                List<String> selectedColors = colors.subList(0, colors.size());
                List<String> inputSizes = new ArrayList<>(dataProperties.getSizes().getTops());

                ProductOption productOption = ProductOption.of(inputSizes, selectedColors, stockRandom, priceRandom);
                Brand brand = new Brand(dataProperties.getBrand().get((int) (Math.random() * dataProperties.getBrand().size())));
                productOption.settingConnection(brand, mainCategory, subCategory, null, ProductStatus.SALE);
                productOptions.add(productOption);
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(1L, TimeUnit.HOURS)) {
            executor.shutdownNow();
        }

        jdbcTemplate.batchUpdate("INSERT INTO brand (name) VALUES (?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, "멋사");
            }

            @Override
            public int getBatchSize() {
                return 1;
            }
        });

        String sql = "INSERT INTO product_option (stock, price, status, common_file_id, main_category_id, sub_category_id, brand_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProductOption productOption = productOptions.get(i);
                ps.setInt(1, productOption.getStock());
                ps.setInt(2, productOption.getPrice());
                ps.setString(3, productOption.getStatus().getText());
                ps.setLong(4, 5);
                ps.setLong(5, 1);
                ps.setLong(6, 1);
                ps.setLong(7, 1);
            }

            @Override
            public int getBatchSize() {
                return productOptions.size();
            }
        });

        String sqlSize = "INSERT INTO size (name, product_option_id) VALUES (?, ?)";
        String sqlColor = "INSERT INTO color (name, product_option_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sqlSize, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, dataProperties.getSizes().getTops().get(i));
                ps.setLong(2, 1);
            }

            @Override
            public int getBatchSize() {
                return dataProperties.getSizes().getTops().size();
            }
        });
        jdbcTemplate.batchUpdate(sqlColor, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, dataProperties.getColors().get(i));
                ps.setLong(2, 1);
            }

            @Override
            public int getBatchSize() {
                return dataProperties.getColors().size();
            }
        });
    }

    public void saveFilesBulk(int count) {
        List<CommonFile> commonFiles = new ArrayList<>();
        for (int idx = 0; idx < count; idx++) {
            List<String> filenames = listS3ObjectKeys(amazonS3Client, amazonProperties.getS3().getBucket());
            CommonFile commonFile = CommonFile.builder()
                    .fileName(selectRandomFilePath(filenames))
                    .build();
            for (int i = 0; i < 5; i++) {
                commonFile.connectFile(CommonFile.builder()
                        .fileName(selectRandomFilePath(filenames))
                        .build());
            }
            commonFiles.add(commonFile);
        }

        String sql = "INSERT INTO common_file (file_name) VALUES (?)";
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    CommonFile commonFile = commonFiles.get(i);
                    ps.setString(1, commonFile.getFileName());
                }

                @Override
                public int getBatchSize() {
                    return count;
                }
            });
    }

    private List<String> listS3ObjectKeys(AmazonS3Client s3Client, String bucketName) {
        ObjectListing objectListing = s3Client.listObjects(bucketName);

        return objectListing.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .toList();
    }

    private String selectRandomFilePath(List<String> filePaths) {
        Random random = new Random();
        String path = filePaths.get(random.nextInt(filePaths.size()));
        while (path.equals("trendpick_logo.png") || path.endsWith("/")) {
            path = filePaths.get(random.nextInt(filePaths.size()));
        }

        return path;
    }
}
