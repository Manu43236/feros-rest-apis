package com.feros.api.repository;

import com.feros.api.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    Optional<StaffProfile> findByUserId(Long userId);

    Optional<StaffProfile> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);

    Optional<StaffProfile> findByUserIdAndIsActiveTrue(Long userId);

    List<StaffProfile> findByTenantIdAndIsActiveTrue(Long tenantId);

    boolean existsByUserIdAndIsActiveTrue(Long userId);
}