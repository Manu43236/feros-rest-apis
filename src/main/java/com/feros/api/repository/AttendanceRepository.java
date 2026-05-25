package com.feros.api.repository;

import com.feros.api.entity.Attendance;
import com.feros.api.enums.AttendanceApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Attendance> findByTenantIdAndApprovalStatusAndIsActiveTrue(Long tenantId, AttendanceApprovalStatus approvalStatus);
    List<Attendance> findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrueOrderByAttendanceDateDesc(
            Long userId, Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT a FROM Attendance a WHERE a.tenant.id = :tenantId AND a.isActive = true AND a.attendanceDate BETWEEN :from AND :to ORDER BY a.user.id, a.attendanceDate ASC")
    List<Attendance> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    Optional<Attendance> findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate date);

    boolean existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrueAndApprovalStatusNot(
            Long userId, Long tenantId, LocalDate date, AttendanceApprovalStatus status);

    boolean existsByUserIdAndTenantIdAndAttendanceDateAndApprovalStatusInAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate date, List<AttendanceApprovalStatus> statuses);

    @Query("SELECT a.user.id FROM Attendance a WHERE a.tenant.id = :tenantId AND a.attendanceDate = :date AND a.approvalStatus IN :statuses AND a.isActive = true")
    List<Long> findUserIdsWithAttendanceOnDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date, @Param("statuses") List<AttendanceApprovalStatus> statuses);
}