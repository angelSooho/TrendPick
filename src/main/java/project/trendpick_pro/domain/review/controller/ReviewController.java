package project.trendpick_pro.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.review.entity.dto.request.ReviewSaveRequest;
import project.trendpick_pro.domain.review.entity.dto.response.ReviewResponse;
import project.trendpick_pro.domain.review.service.ReviewService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/save/{productId}")
    public ResponseEntity<ReviewResponse> createReview(@Valid ReviewSaveRequest reviewCreateRequest,
                                       @MemberEmail String email,
                                       @RequestParam("mainFile") MultipartFile mainFile,
                                       @RequestParam("subFiles") List<MultipartFile> subFiles,
                                       @PathVariable("productId") Long productId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.save(email, productId, reviewCreateRequest, mainFile, subFiles));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> showReview(@PathVariable Long reviewId){
        return ResponseEntity.ok().body(reviewService.getReview(reviewId));
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/edit/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId, ReviewSaveRequest reviewSaveRequest, @RequestParam("mainFile") MultipartFile mainFile,
                               @RequestParam("subFiles") List<MultipartFile> subFiles) {
        return ResponseEntity.ok().body(reviewService.modify(reviewId, reviewSaveRequest, mainFile, subFiles));
    }


    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> showAllReview(Pageable pageable, Model model){
        return ResponseEntity.ok().body(reviewService.getReviews(pageable));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<ReviewResponse>> getOwnReviews(@MemberEmail String email, Pageable pageable){
        return ResponseEntity.ok().body(reviewService.getOwnReviews(email, pageable));
    }
}