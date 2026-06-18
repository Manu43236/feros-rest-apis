package com.feros.api.repository;

import com.feros.api.entity.AttendanceLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceLocationRepository extends JpaRepository<AttendanceLocation, Long> {
    List<AttendanceLocation> findByIsActiveTrue();
}
