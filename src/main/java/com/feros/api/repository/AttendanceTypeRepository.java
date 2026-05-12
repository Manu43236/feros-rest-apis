package com.feros.api.repository;

import com.feros.api.entity.master.AttendanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceTypeRepository extends JpaRepository<AttendanceType, Long> {
    List<AttendanceType> findAllByIsActiveTrue();
    java.util.Optional<AttendanceType> findByNameIgnoreCase(String name);
}