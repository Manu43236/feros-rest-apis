package com.feros.api.repository;

import com.feros.api.entity.master.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Route> findByIdAndTenantId(Long id, Long tenantId);
}