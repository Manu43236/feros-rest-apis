package com.feros.api.repository;

import com.feros.api.entity.master.OwnershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OwnershipTypeRepository extends JpaRepository<OwnershipType, Long> {
    List<OwnershipType> findAllByIsActiveTrue();
    java.util.Optional<OwnershipType> findByNameIgnoreCase(String name);
}