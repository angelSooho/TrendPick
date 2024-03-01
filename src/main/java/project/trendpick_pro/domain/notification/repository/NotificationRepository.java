package project.trendpick_pro.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.trendpick_pro.domain.notification.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Optional<Notification> findByOrderId(Long orderId);
    @Query("select n from Notification n where n.member.email = :email")
    List<Notification> findByMemberEmail(String email);
}
