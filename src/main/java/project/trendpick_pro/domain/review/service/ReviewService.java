package project.trendpick_pro.domain.review.service;

import com.amazonaws.services.s3.AmazonS3Client;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.common.file.FileTranslator;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.review.entity.Review;
import project.trendpick_pro.domain.review.entity.dto.request.ReviewSaveRequest;
import project.trendpick_pro.domain.review.entity.dto.response.ReviewProductResponse;
import project.trendpick_pro.domain.review.entity.dto.response.ReviewResponse;
import project.trendpick_pro.domain.review.repository.ReviewRepository;
import project.trendpick_pro.global.config.AmazonProperties;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final FileTranslator fileTranslator;
    private final MemberService memberService;
    private final ProductService productService;

    private final AmazonS3Client amazonS3;
    private final AmazonProperties amazonProperties;

    @Transactional
    public ReviewResponse save(String email, Long productId, ReviewSaveRequest reviewSaveRequest, MultipartFile requestMainFile, List<MultipartFile> requestSubFiles) {
        Member member = memberService.findByEmail(email);
        Product product = productService.findById(productId);

        CommonFile mainFile = fileTranslator.saveFile(requestMainFile);
        List<CommonFile> subFiles = fileTranslator.saveFiles(requestSubFiles);

        for (CommonFile subFile : subFiles) {
            mainFile.connectFile(subFile);
        }

        Review review = Review.of(reviewSaveRequest, member, product, mainFile);
        product.addReview();
        reviewRepository.save(review);
        return ReviewResponse.of(review);
    }

    @Transactional
    public ReviewResponse modify(Long reviewId, ReviewSaveRequest reviewSaveRequest, MultipartFile requestMainFile, List<MultipartFile> requestSubFiles) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();

        review.getFile().deleteFile(amazonS3, amazonProperties.getS3().getBucket());
        review.disconnectFile();

        CommonFile mainFile = fileTranslator.saveFile(requestMainFile);
        List<CommonFile> subFiles = fileTranslator.saveFiles(requestSubFiles);

        for (CommonFile subFile : subFiles) {
            mainFile.connectFile(subFile);
        }

        review.update(reviewSaveRequest, mainFile);
        return ReviewResponse.of(review);
    }

    @Transactional
    public void delete(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        review.getFile().deleteFile(amazonS3, amazonProperties.getS3().getBucket());
        reviewRepository.delete(review);
    }

    public ReviewResponse getReview(Long productId) {
        Review review = reviewRepository.findById(productId).orElseThrow();
        return ReviewResponse.of(review);
    }

    public Page<ReviewProductResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), 6);
        return reviewRepository.findAllByProductId(productId, pageable);
    }

    @Transactional
    public Page<ReviewResponse> getReviews(Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), 6);
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        return reviewPage.map(ReviewResponse::of);
    }

    @Transactional
    public Page<ReviewResponse> getOwnReviews(String writer, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), 6);
        Page<Review> reviewPage = reviewRepository.findByWriter(writer, pageable);
        return reviewPage.map(ReviewResponse::of);
    }
}
