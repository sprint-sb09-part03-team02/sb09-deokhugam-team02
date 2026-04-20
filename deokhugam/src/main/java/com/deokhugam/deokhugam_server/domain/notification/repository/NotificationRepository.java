package com.deokhugam.deokhugam_server.domain.notification.repository;

import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

    Optional<Notification> findByIdAndIsDeletedFalse(UUID id);

    List<Notification> findAllByUserIdAndIsDeletedFalse(UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    List<Notification> findUnreadByUserId(@Param("userId") UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.isRead = true AND n.createdAt < :threshold AND n.isDeleted = false")
    List<Notification> findExpiredReadNotifications(@Param("threshold") LocalDateTime threshold);
}
