package com.feros.api.repository;

import com.feros.api.entity.WoAmendment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WoAmendmentRepository extends JpaRepository<WoAmendment, Long> {
    List<WoAmendment> findByWorkOrderIdOrderByEffectiveDateDesc(Long workOrderId);
}
