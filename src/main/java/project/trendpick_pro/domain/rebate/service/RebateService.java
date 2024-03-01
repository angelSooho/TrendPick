package project.trendpick_pro.domain.rebate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.cash.entity.CashLog;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.entity.OrderItem;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.domain.rebate.entity.RebateOrderItem;
import project.trendpick_pro.domain.rebate.repository.RebateOrderItemRepository;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RebateService {
    private final RebateOrderItemRepository rebateOrderItemRepository;

    private final OrderService orderService;
    private final MemberService memberService;

    @Transactional
    public void makeDate(String email, String yearMonth) {
        String brand = memberService.findByEmail(email).getBrand();
        generatedDateTime result = getGeneratedDateTime(yearMonth);
        List<OrderItem> orderItems = orderService.getOrdersByCreatedDateBetweenByIdAsc(result.startDateTime(), result.endDateTime());

        List<OrderItem> brandOrderItems = orderItems.stream()
                .filter(item -> {
                    return item.getProduct().getProductOption().getBrand().getName().equals(brand);
                })
                .toList();

        List<RebateOrderItem> rebateOrderItems = brandOrderItems
                .stream()
                .map(this::toRebateOrderItem)
                .toList();

        rebateOrderItems.forEach(this::makeRebateOrderItem);
    }

    @Transactional
    public void makeRebateOrderItem(RebateOrderItem item) {
        RebateOrderItem oldRebateOrderItem = rebateOrderItemRepository.findByOrderItemId(item.getOrderItem().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "정산할 주문품목이 없습니다."));
        if (oldRebateOrderItem != null) {
            if (oldRebateOrderItem.isRebateDone()) {
                return;
            }
            oldRebateOrderItem.updateWith(item);
            rebateOrderItemRepository.save(oldRebateOrderItem);
        } else {
            rebateOrderItemRepository.save(item);
        }
    }

    @Transactional
    public RebateOrderItem toRebateOrderItem(OrderItem orderItem) {
        return new RebateOrderItem(orderItem);
    }

    public List<RebateOrderItem> getRebateOrderItemsByCreatedDateIn(String brandName, String yearMonth) {
        int monthEndDay = YearMonth.parse(yearMonth).lengthOfMonth();
        String fromDateStr = yearMonth + "-01 00:00:00.000000";
        String toDateStr = yearMonth + "-%02d 23:59:59.999999".formatted(monthEndDay);
        LocalDateTime fromDate = LocalDateTime.parse(fromDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        LocalDateTime toDate = LocalDateTime.parse(toDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        return rebateOrderItemRepository.findAllByCreatedDateBetweenAndSellerNameOrderByIdAsc(fromDate, toDate,brandName);
    }

    @Transactional
    public void rebate(long orderItemId) {
        RebateOrderItem rebateOrderItem = rebateOrderItemRepository.findByOrderItemId(orderItemId).get();

        if (!rebateOrderItem.isRebateDone()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 정산된 주문품목입니다.");
        }

        int calculateRebatePrice = rebateOrderItem.calculateRebatePrice();

        CashLog cashLog = memberService.addCash(
                rebateOrderItem.getSellerName(),
                calculateRebatePrice,
                rebateOrderItem.getSeller(),
                CashLog.EvenType.브랜드정산__예치금
        ).getCashLog();

        rebateOrderItem.setRebateDone(cashLog);
    }

    private static generatedDateTime getGeneratedDateTime(String yearMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        // YearMonth 객체를 사용하여 년도와 월 파싱
        YearMonth ym = YearMonth.parse(yearMonth);

        // 해당 월의 첫 번째 날
        LocalDate firstDay = ym.atDay(1);
        LocalDateTime startDateTime = firstDay.atStartOfDay();

        // 해당 월의 마지막 날
        LocalDate lastDay = ym.atEndOfMonth();
        LocalDateTime endDateTime = lastDay.atTime(23, 59, 59, 999999000);

        // 포맷팅된 문자열 출력
        String start = startDateTime.format(formatter);
        String end = endDateTime.format(formatter);
        generatedDateTime result = new generatedDateTime(startDateTime, endDateTime);
        return result;
    }

    private record generatedDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    }
}