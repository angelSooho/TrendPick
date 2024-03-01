package project.trendpick_pro.domain.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.cash.entity.CashLog;
import project.trendpick_pro.domain.cash.repository.CashLogRepository;
import project.trendpick_pro.domain.rebate.entity.RebateOrderItem;
import project.trendpick_pro.domain.withdraw.entity.WithdrawApply;

@Service
@RequiredArgsConstructor
@Transactional
public class CashService {

    private final CashLogRepository cashLogRepository;

    public CashLog addCashLog(WithdrawApply withdrawApply) {
        return cashLogRepository.save(CashLog.of(withdrawApply));
    }

    public CashLog addCashLog(RebateOrderItem rebateOrderItem) {
        return cashLogRepository.save(CashLog.of(rebateOrderItem));
    }
}
