package com.feros.api.repository;

import com.feros.api.entity.master.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findAllByIsActiveTrueOrderByNameAsc();
    Page<State> findByIsActiveTrueOrderByNameAsc(Pageable pageable);
    Page<State> findByIsActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);
}