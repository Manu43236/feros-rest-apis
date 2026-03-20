package com.feros.api.repository;

import com.feros.api.entity.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @Query("SELECT h FROM SubscriptionHistory h WHERE h.endDate < :today AND h.status = 'ACTIVE'")
    List<SubscriptionHistory> findExpiredActive(@Param("today") LocalDate today);

    @Query("SELECT h FROM SubscriptionHistory h WHERE h.endDate = :date AND h.status = 'ACTIVE'")
    List<SubscriptionHistory> findActiveExpiringOn(@Param("date") LocalDate date);
}
