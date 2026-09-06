package com.feros.api.repository;

import com.feros.api.entity.LeaseDriverAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaseDriverAssignmentLogRepository extends JpaRepository<LeaseDriverAssignmentLog, Long> {

    // Current open log for an assignment (unassigned_at is null)
    Optional<LeaseDriverAssignmentLog> findByLeaseVehicleAssignmentIdAndUnassignedAtIsNull(Long assignmentId);

    // Active log for a driver across all assignments (to check isAssigned)
    @Query("""
        SELECT l FROM LeaseDriverAssignmentLog l
        WHERE l.driverStaff.id = :staffId
          AND l.tenant.id = :tenantId
          AND l.unassignedAt IS NULL
        """)
    Optional<LeaseDriverAssignmentLog> findActiveByDriverStaffId(
            @Param("staffId") Long staffId,
            @Param("tenantId") Long tenantId);

    // All active logs for a tenant (bulk load for isAssigned check)
    @Query("""
        SELECT l FROM LeaseDriverAssignmentLog l
        LEFT JOIN FETCH l.driverStaff
        LEFT JOIN FETCH l.leaseVehicleAssignment a
        LEFT JOIN FETCH a.lease
        WHERE l.tenant.id = :tenantId
          AND l.unassignedAt IS NULL
          AND l.driverStaff IS NOT NULL
        """)
    List<LeaseDriverAssignmentLog> findAllActiveByTenantId(@Param("tenantId") Long tenantId);

    // All logs for history — ordered by most recent first
    @Query("""
        SELECT l FROM LeaseDriverAssignmentLog l
        LEFT JOIN FETCH l.leaseVehicleAssignment a
        LEFT JOIN FETCH a.lease
        LEFT JOIN FETCH a.vehicle
        LEFT JOIN FETCH l.driverStaff ds
        LEFT JOIN FETCH ds.user
        LEFT JOIN FETCH l.assignedBy
        WHERE l.tenant.id = :tenantId
        ORDER BY l.assignedAt DESC
        """)
    List<LeaseDriverAssignmentLog> findAllByTenantIdForHistory(@Param("tenantId") Long tenantId);
}
