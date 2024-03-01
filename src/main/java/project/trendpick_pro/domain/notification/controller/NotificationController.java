package project.trendpick_pro.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.notification.entity.dto.NotificationResponse;
import project.trendpick_pro.domain.notification.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(@MemberEmail String email) {
        return ResponseEntity.ok().body(notificationService.findNotificationByMember(email));
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("/{notificationId}")
    public ResponseEntity<Void> remove(@PathVariable("notificationId") Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }
}
