package project.trendpick_pro.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.notification.entity.Notification;
import project.trendpick_pro.domain.notification.entity.dto.NotificationResponse;
import project.trendpick_pro.domain.notification.repository.NotificationRepository;
import project.trendpick_pro.domain.orders.entity.Order;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final OrderService orderService;
    private final MemberService memberService;

    public void saveNotification(String email, Long orderId) {
        Member member = memberService.findByEmail(email);
        Order order=orderService.findById(orderId);
        if(!order.getOrderState().equals("미결제")) {
            Notification notification = Notification.of(member, order);
            notificationRepository.save(notification);
        }
    }

    public NotificationResponse modifyNotification(String email, Long orderId){
        Order order=orderService.findById(orderId);
        Notification notification = notificationRepository.findByOrderId(orderId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "해당 알림이 없습니다."));
        Member member = memberService.findByEmail(email);
        if (notification.getMember() != member) {
            throw new BaseException(ErrorCode.NOT_MATCH, "해당 알림은 본인의 알림이 아닙니다.");
        }

        if (!notification.getNotificationType().equals(order.getOrderState())
                || !notification.getDeliveryState().equals(order.getDeliveryState())) {
            Notification newNotification = Notification.of(notification.getMember(), order);
            notificationRepository.save(newNotification);
        }
        return NotificationResponse.of(notification);
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "해당 알림이 없습니다."));
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findNotificationByMember(String email) {
        List<Notification> notifications = notificationRepository.findByMemberEmail(email);
        return notifications.stream()
                .map(NotificationResponse::of)
                .sorted(Comparator.comparing(NotificationResponse::getCreateDate).reversed())
                .collect(Collectors.toList());
    }
}
