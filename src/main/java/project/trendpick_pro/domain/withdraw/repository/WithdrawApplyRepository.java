package project.trendpick_pro.domain.withdraw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.trendpick_pro.domain.withdraw.entity.WithdrawApply;

import java.util.List;


public interface WithdrawApplyRepository extends JpaRepository<WithdrawApply, Long> {
    @Query("select w from WithdrawApply w where w.member.id = :id")
    List<WithdrawApply> findAllByApplicantId(@Param("id") Long id);

    @Query("select w from WithdrawApply w where w.member.brand = :storeName")
    List<WithdrawApply> findWithdrawsByStoreName(@Param("storeName") String storeName);
}
