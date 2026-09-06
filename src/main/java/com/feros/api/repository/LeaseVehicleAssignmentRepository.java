package com.feros.api.repository;

import com.feros.api.entity.LeaseVehicleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaseVehicleAssignmentRepository extends JpaRepository<LeaseVehicleAssignment, Long> {

    List<LeaseVehicleAssignment> findByLeaseIdOrderByStartDateAsc(Long leaseId);

    Optional<LeaseVehicleAssignment> findByIdAndLeaseId(Long id, Long leaseId);

    // Check if vehicle has any active lease assignment (used to block order assignment)
    @Query("""
        SELECT COUNT(a) > 0 FROM LeaseVehicleAssignment a
        WHERE a.vehicle.id = :vehicleId
          AND a.isActive = true
          AND a.lease.status = com.feros.api.enums.LeaseStatus.ACTIVE
    """)
    boolean existsActiveLeaseForVehicle(@Param("vehicleId") Long vehicleId);

    long countByLeaseId(Long leaseId);

    // Find the active assignment for a vehicle on any active lease (for lease-to-lease transfer)
    @Query("""
        SELECT a FROM LeaseVehicleAssignment a
        WHERE a.vehicle.id = :vehicleId
          AND a.isActive = true
          AND a.lease.status = com.feros.api.enums.LeaseStatus.ACTIVE
    """)
    Optional<LeaseVehicleAssignment> findByLeaseIdAndVehicleIdActive(@Param("vehicleId") Long vehicleId);
}
