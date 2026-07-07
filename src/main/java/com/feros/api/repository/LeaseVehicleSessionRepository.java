package com.feros.api.repository;

import com.feros.api.entity.LeaseVehicleSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaseVehicleSessionRepository extends JpaRepository<LeaseVehicleSession, Long> {

    // All sessions for a lease (all vehicles)
    List<LeaseVehicleSession> findByAssignment_Lease_IdOrderByStartTimeDesc(Long leaseId);

    // All sessions for one vehicle assignment
    List<LeaseVehicleSession> findByAssignmentIdOrderByStartTimeDesc(Long assignmentId);

    // Active session for one vehicle (max 1 at a time)
    Optional<LeaseVehicleSession> findByAssignmentIdAndIsActiveTrue(Long assignmentId);
}
