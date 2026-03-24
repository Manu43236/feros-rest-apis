package com.feros.api.repository;

import com.feros.api.entity.Order;
import com.feros.api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Order> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByOrderNumberAndTenantId(String orderNumber, Long tenantId);
    List<Order> findByTenantIdAndOrderStatusAndIsActiveTrue(Long tenantId, OrderStatus status);
    long countByTenantIdAndIsActiveTrue(Long tenantId);
    long countByTenantIdAndOrderStatusAndIsActiveTrue(Long tenantId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.tenant.id = :tenantId AND o.isActive = true AND o.orderDate BETWEEN :from AND :to ORDER BY o.orderDate DESC")
    List<Order> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT o FROM Order o WHERE o.tenant.id = :tenantId AND o.isActive = true AND o.orderDate BETWEEN :from AND :to AND o.orderStatus = :status ORDER BY o.orderDate DESC")
    List<Order> findByTenantIdAndDateRangeAndStatus(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("status") OrderStatus status);
}