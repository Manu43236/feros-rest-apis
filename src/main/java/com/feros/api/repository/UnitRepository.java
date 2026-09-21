package com.feros.api.repository;

import com.feros.api.entity.master.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findAllByIsActiveTrue();
}
