package project.trendpick_pro.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.product.entity.dto.ProductRequest;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponse;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductResponse;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.global.kafka.view.service.ViewService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ViewService viewService;

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
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok().body(productService.getProduct(productId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> showAllProduct(@RequestParam(value = "page", defaultValue = "0") int offset,
                                 @RequestParam(value = "main-category", defaultValue = "all") String mainCategory,
                                 @RequestParam(value = "sub-category", defaultValue = "all") String subCategory) {
        return ResponseEntity.ok().body(productService.getProducts(offset, mainCategory, subCategory));
    }

    @GetMapping("/keyword")
    public ResponseEntity<Page<ProductListResponse>> searchQuery(@RequestParam(value = "query") String query,
                                                                 @RequestParam(value = "page", defaultValue = "0") int offset) {
        return ResponseEntity.ok().body(productService.findAllByKeyword(query, offset));
    }

    @PostMapping("/admin/discount/{productId}")
    public ResponseEntity<Void> applyDiscount(@PathVariable Long productId, @RequestParam double discountRate) {
        productService.applyDiscount(productId, discountRate);
        return ResponseEntity.ok().build();
    }
}
