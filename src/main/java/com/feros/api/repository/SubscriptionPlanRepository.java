package com.feros.api.repository;

import com.feros.api.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAllByIsActiveTrue();
    Optional<SubscriptionPlan> findByIdAndIsActiveTrue(Long id);
    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);
}
