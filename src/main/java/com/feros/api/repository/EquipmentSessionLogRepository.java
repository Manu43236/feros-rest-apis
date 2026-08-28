package com.feros.api.repository;

import com.feros.api.entity.EquipmentSessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentSessionLogRepository extends JpaRepository<EquipmentSessionLog, Long> {

    List<EquipmentSessionLog> findByOperatorUserIdAndSessionDateOrderByStartTimeAsc(Long userId, LocalDate date);

    // For today's home screen — all sessions regardless of open/closed
    List<EquipmentSessionLog> findByOperatorUserIdAndSessionDateOrderByStartTimeDesc(Long userId, LocalDate date);

    // Last closed session with an HMR reading (for start HMR pre-fill)
    Optional<EquipmentSessionLog> findFirstByMachineAssignment_IdAndEndHmrNotNullOrderByEndTimeDesc(Long assignmentId);

    // Guard: ensure operator owns the session before delete/close
    Optional<EquipmentSessionLog> findByIdAndOperatorUserId(Long id, Long operatorUserId);
}
