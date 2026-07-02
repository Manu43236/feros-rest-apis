package com.feros.api.repository;

import com.feros.api.entity.DailyLogDivision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyLogDivisionRepository extends JpaRepository<DailyLogDivision, Long> {
    List<DailyLogDivision> findByDailyLogId(Long dailyLogId);
}
