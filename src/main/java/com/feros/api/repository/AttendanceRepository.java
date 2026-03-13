package com.feros.api.repository;

import com.feros.api.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<Attendance> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);
    List<Attendance> findByTenantIdAndAttendanceDateAndIsActiveTrue(Long tenantId, LocalDate date);
    List<Attendance> findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate from, LocalDate to);
    Optional<Attendance> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate date);
}