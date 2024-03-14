package project.trendpick_pro.domain.withdraw.entity;

import jakarta.persistence.*;
import lombok.*;
import project.trendpick_pro.domain.cash.entity.CashLog;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.withdraw.entity.dto.WithDrawApplyForm;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawApply {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Member member;

    private String bankName;
    private String bankAccountNo;
    private int price;

    @ManyToOne(fetch = LAZY)
    private CashLog withdrawCashLog; // 출금에 관련된 내역

    private LocalDateTime withdrawDate;
    private LocalDateTime cancelDate;
    private String msg;

    @Builder
    public WithdrawApply(Member member, String bankName, String bankAccountNo, int price, CashLog withdrawCashLog, LocalDateTime withdrawDate, LocalDateTime cancelDate, String msg) {
        this.member = member;
        this.bankName = bankName;
        this.bankAccountNo = bankAccountNo;
        this.price = price;
        this.withdrawCashLog = withdrawCashLog;
        this.withdrawDate = withdrawDate;
        this.cancelDate = cancelDate;
        this.msg = msg;
    }

    static public WithdrawApply of(WithDrawApplyForm withDrawApplyForm, Member member){
        return WithdrawApply.builder()
                .bankName(withDrawApplyForm.getBankName())
                .bankAccountNo(withDrawApplyForm.getBankAccountNo())
                .price(withDrawApplyForm.getPrice())
                .member(member)
                .build();
    }

    public boolean checkAlreadyProcessed() {
        return withdrawDate != null || withdrawCashLog != null || cancelDate != null;
    }

    public void setApplyDone(CashLog cashLog, String msg) {
        withdrawDate = LocalDateTime.now();
        this.withdrawCashLog = cashLog;
        this.msg = msg;
    }

    public void setCancelDone(String msg) {
        cancelDate = LocalDateTime.now();
        this.msg = msg;
    }

    public boolean isApplyDone() {
        return withdrawDate != null;
    }

    public boolean isCancelDone() {
        return cancelDate != null;
    }
}
