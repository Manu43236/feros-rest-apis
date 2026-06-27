package com.feros.api.repository;

import com.feros.api.entity.ClientDivision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientDivisionRepository extends JpaRepository<ClientDivision, Long> {
    List<ClientDivision> findByClientIdAndIsActiveTrueOrderByNameAsc(Long clientId);
    Optional<ClientDivision> findByIdAndClientId(Long id, Long clientId);
    boolean existsByNameIgnoreCaseAndClientId(String name, Long clientId);
}
