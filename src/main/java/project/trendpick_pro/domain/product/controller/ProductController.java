package project.trendpick_pro.domain.product.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.product.entity.dto.ProductRequest;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponse;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponseBySeller;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductResponse;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.global.kafka.view.service.ViewService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final MemberService memberService;
    private final ProductService productService;
    private final RecommendService recommendService;

    private final ViewService viewService;

    @Value("${colors}")
    private List<String> colors;
    @Value("${sizes.tops}")
    private List<Integer> tops;

    @Value("${sizes.bottoms}")
    private List<Integer> bottoms;

    @Value("${sizes.shoes}")
    private List<Integer> shoes;

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @PostMapping("/save")
    public ResponseEntity<Long> save(@ModelAttribute @Valid ProductRequest productRequest,
                               @RequestParam("mainFile") MultipartFile mainFile,
                               @RequestParam("subFiles") List<MultipartFile> subFiles) {
        return ResponseEntity.ok().body(productService.saveProduct(productRequest, mainFile, subFiles));
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @PostMapping("/modify/{productId}")
    public ResponseEntity<Long> modifyProduct(@PathVariable Long productId, @Valid ProductRequest productRequest,
                                              @RequestParam("mainFile") MultipartFile mainFile,
                                              @RequestParam("subFiles") List<MultipartFile> subFiles) throws IOException {
        return ResponseEntity.ok().body(productService.modifyProduct(productId, productRequest, mainFile, subFiles));
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@MemberEmail String email, @PathVariable Long productId, Pageable pageable) {
        return ResponseEntity.ok().body(productService.getProduct(email, productId, pageable));
    }

    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> showAllProduct(@RequestParam(value = "page", defaultValue = "0") int offset,
                                 @RequestParam(value = "main-category", defaultValue = "all") String mainCategory,
                                 @RequestParam(value = "sub-category", defaultValue = "all") String subCategory, HttpSession session) {
        viewService.requestIncrementViewCount(session);
        return ResponseEntity.ok().body(productService.getProducts(offset, mainCategory, subCategory));
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @GetMapping("/admin")
    public ResponseEntity<Page<ProductListResponseBySeller>> showAllProductBySeller(@MemberEmail String email, @RequestParam("page") int offset) {
        return ResponseEntity.ok().body(productService.findProductsBySeller(email, offset));
    }

    @GetMapping("/keyword")
    public ResponseEntity<Page<ProductListResponse>> searchQuery(@RequestParam String keyword,
                                                                 @RequestParam(value = "page", defaultValue = "0") int offset) {
        return ResponseEntity.ok().body(productService.findAllByKeyword(keyword, offset));
    }

    @PostMapping("/admin/discount/{productId}")
    public ResponseEntity<Void> applyDiscount(@PathVariable Long productId, @RequestParam double discountRate) {
        productService.applyDiscount(productId, discountRate);
        return ResponseEntity.ok().build();
    }
}
