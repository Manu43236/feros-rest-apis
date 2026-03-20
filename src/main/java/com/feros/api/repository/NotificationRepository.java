package com.feros.api.repository;

import com.feros.api.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE (n.tenant.id = :tenantId OR n.tenant IS NULL) AND (n.userId = :userId OR n.userId IS NULL) ORDER BY n.createdAt DESC")
    List<Notification> findForUser(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.tenant.id = :tenantId OR n.tenant IS NULL) AND (n.userId = :userId OR n.userId IS NULL) AND n.isRead = false")
    long countUnread(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE (n.tenant.id = :tenantId OR n.tenant IS NULL) AND (n.userId = :userId OR n.userId IS NULL)")
    void markAllRead(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
