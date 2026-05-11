package com.feros.api.repository;

import com.feros.api.entity.UpgradeRequest;
import com.feros.api.enums.UpgradeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, Long> {
    List<UpgradeRequest> findAllByStatusOrderByCreatedAtDesc(UpgradeRequestStatus status);
    List<UpgradeRequest> findAllByOrderByCreatedAtDesc();
}
