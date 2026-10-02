package com.feros.api.repository;

import com.feros.api.entity.SubscriptionAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionAddonRepository extends JpaRepository<SubscriptionAddon, Long> {

    List<SubscriptionAddon> findBySubscriptionHistoryId(Long subscriptionHistoryId);

    @Query("SELECT COALESCE(SUM(a.addonVehicleCount), 0) FROM SubscriptionAddon a " +
           "WHERE a.subscriptionHistory.id = :historyId AND a.status = 'ACTIVE'")
    int sumActiveAddonVehicleCount(@Param("historyId") Long historyId);
}
