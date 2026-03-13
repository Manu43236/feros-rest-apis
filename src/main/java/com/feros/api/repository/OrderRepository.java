package com.feros.api.repository;

import com.feros.api.entity.Order;
import com.feros.api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Order> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByOrderNumberAndTenantId(String orderNumber, Long tenantId);
    List<Order> findByTenantIdAndOrderStatusAndIsActiveTrue(Long tenantId, OrderStatus status);
}