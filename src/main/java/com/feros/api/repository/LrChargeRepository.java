package com.feros.api.repository;

import com.feros.api.entity.LrCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LrChargeRepository extends JpaRepository<LrCharge, Long> {
    List<LrCharge> findByLrIdAndIsActiveTrue(Long lrId);
}