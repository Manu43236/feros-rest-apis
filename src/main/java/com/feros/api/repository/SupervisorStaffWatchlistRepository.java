package com.feros.api.repository;

import com.feros.api.entity.SupervisorStaffWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorStaffWatchlistRepository extends JpaRepository<SupervisorStaffWatchlist, Long> {

    List<SupervisorStaffWatchlist> findByTenantIdAndSupervisorId(Long tenantId, Long supervisorId);

    Optional<SupervisorStaffWatchlist> findByTenantIdAndSupervisorIdAndStaffUserId(
            Long tenantId, Long supervisorId, Long userId);

    boolean existsByTenantIdAndSupervisorIdAndStaffUserId(Long tenantId, Long supervisorId, Long userId);

    void deleteByTenantIdAndSupervisorIdAndStaffUserId(Long tenantId, Long supervisorId, Long userId);

    @Query("SELECT w.staffUser.id FROM SupervisorStaffWatchlist w WHERE w.tenant.id = :tenantId AND w.supervisor.id = :supervisorId")
    List<Long> findUserIdsByTenantIdAndSupervisorId(Long tenantId, Long supervisorId);
}
