package com.feros.api.repository;

import com.feros.api.entity.MachineConditionSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineConditionSurveyRepository extends JpaRepository<MachineConditionSurvey, Long> {
    List<MachineConditionSurvey> findByMachineAssignmentIdAndIsActiveTrueOrderBySurveyDateDesc(Long machineAssignmentId);
}
