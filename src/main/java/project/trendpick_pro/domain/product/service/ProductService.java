package project.trendpick_pro.domain.product.service;


import com.querydsl.core.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.brand.entity.Brand;
import project.trendpick_pro.domain.brand.repository.BrandRepository;
import project.trendpick_pro.domain.category.entity.MainCategory;
import project.trendpick_pro.domain.category.entity.SubCategory;
import project.trendpick_pro.domain.category.repository.MainCategoryRepository;
import project.trendpick_pro.domain.category.repository.SubCategoryRepository;
import project.trendpick_pro.domain.common.base.filetranslator.FileTranslator;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.product.entity.dto.response.ProductByRecommended;
import project.trendpick_pro.domain.product.exception.ProductNotFoundException;
import project.trendpick_pro.domain.tags.favoritetag.entity.FavoriteTag;
import project.trendpick_pro.domain.tags.favoritetag.service.FavoriteTagService;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.RoleType;
import project.trendpick_pro.domain.member.exception.MemberNotFoundException;
import project.trendpick_pro.domain.member.exception.MemberNotMatchException;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.product.entity.Product;
import project.trendpick_pro.domain.product.entity.dto.request.ProductSaveRequest;
import project.trendpick_pro.domain.product.entity.dto.request.ProductSearchCond;
import project.trendpick_pro.domain.product.entity.dto.response.ProductListResponse;
import project.trendpick_pro.domain.product.entity.dto.response.ProductResponse;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.tags.tag.entity.Tag;
import project.trendpick_pro.domain.tags.tag.entity.type.TagType;
import project.trendpick_pro.domain.tags.tag.service.TagService;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BrandRepository brandRepository;
    private final FileTranslator fileTranslator;
    private final FavoriteTagService favoriteTagService;
    private final TagService tagService;

    @Value("${file.dir}")
    private String filePath;

    @Transactional
    public ProductResponse register(ProductSaveRequest productSaveRequest, MultipartFile requestMainFile, List<MultipartFile> requestSubFiles) throws IOException {

        CheckMember();

        CommonFile mainFile = fileTranslator.translateFile(requestMainFile);
        List<CommonFile> subFiles = fileTranslator.translateFileList(requestSubFiles);

        for (CommonFile subFile : subFiles) {
            mainFile.connectFile(subFile);
        }

        Set<Tag> tags = new LinkedHashSet<>();  // 상품에 포함시킬 태크 선택하여 저장
        for (String tagName : productSaveRequest.tags()) {
//            tags.add(tagRepository.findByName(tag).orElseThrow());
            tags.add(new Tag(tagName));
        }

        MainCategory mainCategory = mainCategoryRepository.findByName(productSaveRequest.mainCategory());
        SubCategory subCategory = subCategoryRepository.findByName(productSaveRequest.subCategory());
        Brand brand = brandRepository.findByName(productSaveRequest.brand());

        Product product = Product.of(productSaveRequest, mainCategory, subCategory, brand, mainFile, tags);

        productRepository.save(product);
        return ProductResponse.of(product);
    }

    @Transactional
    public ProductResponse modify(Long productId, ProductSaveRequest productSaveRequest, MultipartFile requestMainFile, List<MultipartFile> requestSubFiles) throws IOException {

        CheckMember();

        Product product = productRepository.findById(productId).orElseThrow(null);// 임시. 나중에 테스트
        CommonFile mainFile = product.getFile();
        List<CommonFile> subFiles = product.getFile().getChild();

        if(requestMainFile!=null){
            //  기존 이미지 삭제
            FileUtils.delete(new File(mainFile.getFileName()));
        }
        // 이미지 업데이트
        mainFile = fileTranslator.translateFile(requestMainFile);

        if(requestSubFiles!=null ){
            // 기존 이미지 삭제
            for(CommonFile subFile:subFiles){
                FileUtils.delete(new File(subFile.getFileName()));
            }
        }
        // 이미지 업데이트
        subFiles=fileTranslator.translateFileList(requestSubFiles);

        for (CommonFile subFile : subFiles) {
            mainFile.connectFile(subFile);
        }
        product.update(productSaveRequest);

        return ProductResponse.of(product);
    }

    @Transactional
    public void delete(Long productId) {

        CheckMember();

        Product product = productRepository.findById(productId).orElseThrow(null);// 임시. 나중에 테스트
        productRepository.delete(product);
    }

    public ProductResponse show(Long product_id) {
        Product product = productRepository.findById(product_id).orElseThrow(null);// 임시. 나중에 테스트

        Member member = CheckMember();
        favoriteTagService.updateTag(member, product, TagType.SHOW);

        return ProductResponse.of(product);
    }

    public Page<ProductListResponse> showAll(int offset, String mainCategory, String subCategory, Integer sortCode) {

        ProductSearchCond cond = new ProductSearchCond(mainCategory, subCategory, sortCode);
        PageRequest pageable = PageRequest.of(offset, 18);

        Page<ProductListResponse> listResponses = productRepository.findAllByCategoryId(cond, pageable);

        List<ProductListResponse> list = listResponses.getContent().stream()
                .peek(product -> {
                    String updatedMainFile = filePath + product.getMainFile();
                    product.setMainFile(updatedMainFile);
                }).toList();

        return new PageImpl<>(list, pageable, listResponses.getTotalElements());
    }


    private Member CheckMember() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName(); // 둘다 테스트 해보기
        Member member = memberRepository.findByEmail(username).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));

        if (member.getRole().equals(RoleType.MEMBER)) {
            throw new MemberNotMatchException("허용된 권한이 아닙니다.");
        }
        return member;
    }

    public List<Product> getRecommendProduct(Member member){

        List<ProductByRecommended> tags = productRepository.findRecommendProduct(member.getUsername());
        Set<FavoriteTag> memberTags = member.getTags();


        //태그명에 따라 가지고 있는 product_id
        // : 멤버 태그명에 따라 해당 상품에 점수를 부여해야 하기 때문에
        Map<String, List<Long>> productIdListByTagName = new HashMap<>();

        //상품 id 중복을 없애기 위함
        //맴버의 태그명과 여러개가 겹쳐서 여러개의 추천상품이 반환되었을것 그 중복을 없애야 한다.
        Map<Long, ProductByRecommended> recommendProductByProductId = new HashMap<>();

        //같은 태그명을 가지고 있지만 제각각 상품을 가르키는 productId는 다를 것이다. 그래서 태그명 별로 어떤 상품들을 가르키는지 모아보자
        for (ProductByRecommended tag : tags) {
            if(!productIdListByTagName.containsKey(tag.getTagName()))
                productIdListByTagName.put(tag.getTagName(), new ArrayList<>());
            productIdListByTagName.get(tag.getTagName()).add(tag.getProductId());
        }

        //마찬가지로 같은 상품을 가르키고 있지만 태그명은 제각각일 것이다. 우리가 뽑아내길 원하는 것은 추천상품이다. 즉 같은 상품이 중복되면 안된다.
        //그래서 상품Id에 대한 중복을 없애서 하나에 몰아넣는 코드이다.
        for (ProductByRecommended response : tags) {
            if(recommendProductByProductId.containsKey(response.getProductId()))
                continue;
            recommendProductByProductId.put(response.getProductId(), response);
        }

        //실제로직! member 선호태그에는 점수가 있을 것이다.
        //그러니까  우리가 반환하려고 하는 추천상품이 점수가 몇점인지 갱신하는 코드이다.
        for (FavoriteTag memberTag : memberTags) {
            if(productIdListByTagName.containsKey(memberTag.getName())){
                List<Long> productIdList = productIdListByTagName.get(memberTag.getName());
                for (Long id : productIdList) {
                    recommendProductByProductId.get(id).plusTotalScore(memberTag.getScore());
                }
            }
        }

        List<ProductByRecommended> recommendProductList = new ArrayList<>(recommendProductByProductId.values()).stream()
                .sorted(Comparator.comparing(ProductByRecommended::getTotalScore).reversed())
                .toList();

        //Product 변환해서 리턴
        List<Product> products = new ArrayList<>();
        for(ProductByRecommended recommendProduct : recommendProductList){
            products.add(productRepository.findById(recommendProduct.getProductId()).orElseThrow(
                    () -> new ProductNotFoundException("존재하지 않는 상품입니다.")
            ));
        }

        return products;
    }

}
