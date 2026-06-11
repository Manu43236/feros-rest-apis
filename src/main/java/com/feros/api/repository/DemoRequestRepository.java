package com.feros.api.repository;

import com.feros.api.entity.DemoRequest;
import com.feros.api.enums.DemoRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {
    Page<DemoRequest> findByStatus(DemoRequestStatus status, Pageable pageable);
    long countByStatus(DemoRequestStatus status);
}
