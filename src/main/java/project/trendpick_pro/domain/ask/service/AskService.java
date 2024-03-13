package project.trendpick_pro.domain.ask.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.ask.entity.Ask;
import project.trendpick_pro.domain.ask.entity.dto.form.AskRequest;
import project.trendpick_pro.domain.ask.entity.dto.response.AskResponse;
import project.trendpick_pro.domain.ask.repository.AskRepository;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskService {

    private final AskRepository askRepository;
    private final ProductService productService;

    private final MemberService memberService;

    @Transactional
    public Long saveAsk(String email, AskRequest askRequest) {
        Member member = memberService.findByEmail(email);
        Product product = productService.findById(askRequest.getProductId());
        Ask savedAsk = askRepository.save(Ask.of(askRequest.getTitle(), askRequest.getContent()));
        savedAsk.connectProduct(product);
        savedAsk.connectMember(member);
        return savedAsk.getId();
    }

    @Transactional
    public AskResponse modify(String email, Long askId, AskRequest askRequest) {
        Member member = memberService.findByEmail(email);
        Ask ask = getAskWithAuthValidation(member, askId);
        ask.update(askRequest);
        return AskResponse.of(ask);
    }

    @Transactional
    public void delete(String email, Long askId) {
        Member member = memberService.findByEmail(email);
        Ask ask = getAskWithAuthValidation(member, askId);
        askRepository.delete(ask);
    }

    public AskResponse getAsk(Long askId) {
        return AskResponse.of(
            askRepository.findById(askId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "해당 문의글은 존재하지 않습니다."))
        );
    }

    public Page<AskResponse> findAsksByProduct(Long productId, int offset) {
        Pageable pageable = PageRequest.of(offset, 5);
        return askRepository.findAllByProductId(productId, pageable);
    }

    private Ask getAskWithAuthValidation(Member member, Long askId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() ->new BaseException(ErrorCode.NOT_FOUND, "해당 문의글은 존재하지 않습니다."));
        if (!Objects.equals(ask.getAuthor().getId(), member.getId())) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "해당 문의글은 존재하지 않습니다.");
        }
        return ask;
    }
}
