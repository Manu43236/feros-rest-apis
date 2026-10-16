package com.feros.api.repository;

import com.feros.api.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    List<OrderPayment> findByOrderIdAndIsActiveTrueOrderByTxnDateAscIdAsc(Long orderId);

    Optional<OrderPayment> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
