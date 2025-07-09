package com.bytebites.notificationservice.repository;

import com.bytebites.notificationservice.enums.NotificationStatus;
import com.bytebites.notificationservice.enums.NotificationType;
import com.bytebites.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByEventId(String eventId);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    List<Notification> findByTypeAndStatusOrderByCreatedAtDesc(NotificationType type, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :startTime AND n.status = :status")
    List<Notification> findRecentNotificationsByStatus(@Param("startTime") LocalDateTime startTime,
                                                       @Param("status") NotificationStatus status);

    long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime after);
}
