package com.feros.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.feros.api.entity.Designation;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    List<Designation> findByTenantIdAndIsActiveTrue(Long tenantId);

    Optional<Designation> findByIdAndTenantId(Long id, Long tenantId);

}