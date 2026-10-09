package com.feros.api.repository;

import com.feros.api.entity.LeaseCleanerAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Mirror of {@link LeaseDriverAssignmentLogRepository} for cleaners. */
@Repository
public interface LeaseCleanerAssignmentLogRepository extends JpaRepository<LeaseCleanerAssignmentLog, Long> {

    Optional<LeaseCleanerAssignmentLog> findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(Long assignmentId);

    @Query("""
        SELECT l FROM LeaseCleanerAssignmentLog l
        WHERE l.leaseVehicleAssignment.lease.id = :leaseId
          AND l.unassignedAt IS NULL
        """)
    List<LeaseCleanerAssignmentLog> findOpenByLeaseId(@Param("leaseId") Long leaseId);

    @Query("""
        SELECT l FROM LeaseCleanerAssignmentLog l
        WHERE l.cleanerStaff.id = :staffId
          AND l.tenant.id = :tenantId
          AND l.unassignedAt IS NULL
        """)
    Optional<LeaseCleanerAssignmentLog> findActiveByCleanerStaffId(
            @Param("staffId") Long staffId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT l FROM LeaseCleanerAssignmentLog l
        LEFT JOIN FETCH l.cleanerStaff cs
        LEFT JOIN FETCH cs.user
        LEFT JOIN FETCH l.leaseVehicleAssignment a
        LEFT JOIN FETCH a.vehicle
        WHERE l.tenant.id = :tenantId
          AND l.unassignedAt IS NULL
          AND l.cleanerStaff IS NOT NULL
        """)
    List<LeaseCleanerAssignmentLog> findAllActiveByTenantId(@Param("tenantId") Long tenantId);

    @Query("""
        SELECT l FROM LeaseCleanerAssignmentLog l
        JOIN FETCH l.leaseVehicleAssignment a
        JOIN FETCH a.vehicle
        JOIN FETCH l.cleanerStaff cs
        JOIN FETCH cs.user
        WHERE l.tenant.id = :tenantId
          AND l.cleanerStaff IS NOT NULL
          AND l.assignedAt <= :endDate
          AND (l.unassignedAt IS NULL OR l.unassignedAt >= :startDate)
        """)
    List<LeaseCleanerAssignmentLog> findOverlappingByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
