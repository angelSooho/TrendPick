package project.trendpick_pro.global.tosspayment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.notification.service.NotificationService;
import project.trendpick_pro.domain.orders.entity.Order;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.global.tosspayment.dto.PaymentResultResponse;
import project.trendpick_pro.global.tosspayment.service.PaymentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @Transactional
    @GetMapping(value = "/{id}/success")
    public ResponseEntity<String> PaymentTry(
            @PathVariable("id") Long id,
            @RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "amount") Integer amount,
            @RequestParam(value = "paymentKey") String paymentKey,
            @MemberEmail String email) {

        PaymentResultResponse response = paymentService.requestPayment(paymentKey, orderId, amount);
        Order order = orderService.findById(id);

        if (response.getStatus().equals("DONE")) {
            order.connectPayment(
                    "TossPayments" + response.getMethod(),
                    response.getPaymentKey()
            );
            notificationService.saveNotification(email, order.getId());
            return ResponseEntity.ok("결제가 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("결제에 실패했습니다.");
        }
    }

    @GetMapping(value = "/{id}/cancel")
    public ResponseEntity<String> paymentCancel(@PathVariable("id") Long id, @MemberEmail String email) {
        orderService.cancel(id);
        notificationService.saveNotification(email, id);
        paymentService.cancelPayment(orderService.findById(id).getPaymentKey());
        return ResponseEntity.ok("결제가 취소되었습니다.");
    }
}
