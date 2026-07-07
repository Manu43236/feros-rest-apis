package com.feros.api.repository;

import com.feros.api.entity.master.PartCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartCategoryRepository extends JpaRepository<PartCategory, Long> {
    List<PartCategory> findAllByIsActiveTrue();
}
