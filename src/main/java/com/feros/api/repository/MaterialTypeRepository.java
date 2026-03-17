package com.feros.api.repository;

import com.feros.api.entity.master.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialTypeRepository extends JpaRepository<MaterialType, Long> {
    List<MaterialType> findAllByIsActiveTrue();
    Optional<MaterialType> findByNameIgnoreCase(String name);
}