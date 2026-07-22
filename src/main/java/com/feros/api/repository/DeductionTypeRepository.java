package com.feros.api.repository;

import com.feros.api.entity.master.DeductionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeductionTypeRepository extends JpaRepository<DeductionType, Long> {
    List<DeductionType> findAllByIsActiveTrue();
    java.util.Optional<DeductionType> findByNameIgnoreCaseAndIsActiveTrue(String name);
}