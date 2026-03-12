package com.feros.api.repository;

import com.feros.api.entity.master.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {
    List<EmploymentType> findAllByIsActiveTrue();
}