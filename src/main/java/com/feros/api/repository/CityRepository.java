package com.feros.api.repository;

import com.feros.api.entity.master.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByStateIdAndIsActiveTrueOrderByNameAsc(Long stateId);
    Page<City> findByIsActiveTrueOrderByNameAsc(Pageable pageable);
    Page<City> findByIsActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);
    Page<City> findByStateIdAndIsActiveTrueOrderByNameAsc(Long stateId, Pageable pageable);
    Page<City> findByStateIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(Long stateId, String name, Pageable pageable);
}