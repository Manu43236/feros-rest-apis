package com.feros.api.repository;

import com.feros.api.entity.LeaseDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaseDailyLogRepository extends JpaRepository<LeaseDailyLog, Long> {
    List<LeaseDailyLog> findByLeaseIdOrderByLogDateDesc(Long leaseId);
    boolean existsByAssignmentIdAndLogDate(Long assignmentId, LocalDate logDate);
}
