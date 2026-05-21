package com.feros.api.repository;

import com.feros.api.entity.master.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByStateIdAndIsActiveTrueOrderByNameAsc(Long stateId);
}