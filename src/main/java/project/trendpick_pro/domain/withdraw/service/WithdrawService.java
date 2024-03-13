package project.trendpick_pro.domain.withdraw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.brand.service.BrandService;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.withdraw.entity.WithdrawApply;
import project.trendpick_pro.domain.withdraw.repository.WithdrawApplyRepository;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawService {

    private final WithdrawApplyRepository withdrawApplyRepository;

    private final MemberService memberService;
    private final BrandService brandService;

    @Transactional
    public WithdrawApply apply(String bankName, String bankAccountNo, Integer price, Member applicant) {
        WithdrawApply withdrawApply = WithdrawApply.builder()
                .bankName(bankName)
                .bankAccountNo(bankAccountNo)
                .price(price)
                .applicant(applicant)
                .build();
        withdrawApplyRepository.save(withdrawApply);
        return withdrawApply;
    }

    public List<WithdrawApply> getWithdraws(String email){
        Member member = memberService.findByEmail(email);
        if (member.getRole().getValue().equals("ADMIN")) {
            return withdrawApplyRepository.findAll();
        }
        return withdrawApplyRepository.findAllByApplicantId(member.getId());
    }

//    @Transactional
//    public void withdraw(Long withdrawApplyId) {
//        WithdrawApply withdrawApply = withdrawApplyRepository.findById(withdrawApplyId).orElseThrow(() -> new BaseException(ErrorCode.BAD_REQUEST, "출금신청 데이터를 찾을 수 없습니다."));
//        long restCash = memberService.getRestCash(withdrawApply.getApplicant());
//
//        if (!withdrawApply.isApplyDone()) {
//            throw new BaseException(ErrorCode.BAD_REQUEST, "출금신청이 처리되지 않았습니다.");
//        }
//        if (withdrawApply.getPrice() > restCash) {
//            throw new BaseException(ErrorCode.BAD_REQUEST, "출금 요청 금액은 잔여 캐시보다 많을 수 없습니다.");
//        }
//        Brand brand=brandService.findByName(withdrawApply.getApplicant().getBrand());
//
//        CashLog cashLog = memberService.addCash(
//                        withdrawApply.getApplicant().getBrand(),
//                        withdrawApply.getPrice() * -1,
//                        brand,
//                        CashLog.EvenType.출금__통장입금).getCashLog();
//
//        withdrawApply.setApplyDone(cashLog, "관리자에 의해서 처리되었습니다.");
//    }

    @Transactional
    public void cancelApply(Long withdrawApplyId) {
        WithdrawApply withdrawApply = withdrawApplyRepository.findById(withdrawApplyId).orElseThrow(() -> new BaseException(ErrorCode.BAD_REQUEST, "출금신청 데이터를 찾을 수 없습니다."));

        if (!withdrawApply.isApplyDone()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "출금신청이 처리되지 않았습니다.");
        }

        withdrawApply.setCancelDone("관리자에 의해서 취소되었습니다.");
    }
}
