package com.feros.api.repository;

import com.feros.api.entity.SupervisorVehicleWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorVehicleWatchlistRepository extends JpaRepository<SupervisorVehicleWatchlist, Long> {

    List<SupervisorVehicleWatchlist> findByTenantIdAndSupervisorId(Long tenantId, Long supervisorId);

    Optional<SupervisorVehicleWatchlist> findByTenantIdAndSupervisorIdAndVehicleId(
            Long tenantId, Long supervisorId, Long vehicleId);

    boolean existsByTenantIdAndSupervisorIdAndVehicleId(Long tenantId, Long supervisorId, Long vehicleId);

    void deleteByTenantIdAndSupervisorIdAndVehicleId(Long tenantId, Long supervisorId, Long vehicleId);

    @Query("SELECT w.vehicle.id FROM SupervisorVehicleWatchlist w WHERE w.tenant.id = :tenantId AND w.supervisor.id = :supervisorId")
    List<Long> findVehicleIdsByTenantIdAndSupervisorId(Long tenantId, Long supervisorId);
}
