package com.feros.api.repository;

import com.feros.api.entity.LeaseShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaseShiftAssignmentRepository extends JpaRepository<LeaseShiftAssignment, Long> {

    // Upsert key: one row per (vehicle assignment, shift, day)
    Optional<LeaseShiftAssignment> findByLeaseVehicleAssignmentIdAndLeaseShiftIdAndWorkDate(
            Long leaseVehicleAssignmentId, Long leaseShiftId, LocalDate workDate);

    // Same-date assignments for a staff member (+shift windows) — for the time-overlap guard
    @Query("""
        SELECT a FROM LeaseShiftAssignment a JOIN FETCH a.leaseShift
        WHERE a.driverStaff.id = :staffId AND a.workDate = :date
        """)
    List<LeaseShiftAssignment> findByDriverStaffIdAndWorkDate(
            @Param("staffId") Long staffId, @Param("date") LocalDate date);

    @Query("""
        SELECT a FROM LeaseShiftAssignment a JOIN FETCH a.leaseShift
        WHERE a.cleanerStaff.id = :staffId AND a.workDate = :date
        """)
    List<LeaseShiftAssignment> findByCleanerStaffIdAndWorkDate(
            @Param("staffId") Long staffId, @Param("date") LocalDate date);

    // Grid for the lease detail page
    @Query("""
        SELECT a FROM LeaseShiftAssignment a
        JOIN FETCH a.leaseShift s
        JOIN FETCH a.leaseVehicleAssignment lva
        JOIN FETCH lva.vehicle
        LEFT JOIN FETCH a.driverStaff ds LEFT JOIN FETCH ds.user
        LEFT JOIN FETCH a.cleanerStaff cs LEFT JOIN FETCH cs.user
        WHERE lva.lease.id = :leaseId
          AND a.workDate BETWEEN :from AND :to
        ORDER BY a.workDate ASC, s.shiftNumber ASC
        """)
    List<LeaseShiftAssignment> findByLeaseIdAndWorkDateBetween(
            @Param("leaseId") Long leaseId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Tenant-wide range load for the three vehicle-day resolvers + shift-pay resolver.
    // Includes client shifts (null staff) so the vehicle-cost report can show ₹0 lines.
    @Query("""
        SELECT a FROM LeaseShiftAssignment a
        JOIN FETCH a.leaseShift s
        JOIN FETCH a.leaseVehicleAssignment lva
        JOIN FETCH lva.vehicle
        LEFT JOIN FETCH a.driverStaff ds LEFT JOIN FETCH ds.user
        LEFT JOIN FETCH a.cleanerStaff cs LEFT JOIN FETCH cs.user
        WHERE a.tenant.id = :tenantId
          AND a.workDate BETWEEN :from AND :to
        """)
    List<LeaseShiftAssignment> findOverlappingByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Future shift rows for ONE vehicle assignment — dropped on per-vehicle close so a
    // closed vehicle can never pin its driver/cleaner or block their reassignment.
    List<LeaseShiftAssignment> findByLeaseVehicleAssignmentIdAndWorkDateGreaterThanEqual(
            Long leaseVehicleAssignmentId, LocalDate workDate);
}
