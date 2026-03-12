package com.feros.api.repository;

import com.feros.api.entity.master.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findAllByIsActiveTrue();
}