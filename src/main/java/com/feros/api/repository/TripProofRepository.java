package com.feros.api.repository;

import com.feros.api.entity.TripProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripProofRepository extends JpaRepository<TripProof, Long> {
    List<TripProof> findByLrIdAndIsActiveTrue(Long lrId);

    List<TripProof> findByUserIdAndIsActiveTrue(Long userId);

    Optional<TripProof> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    List<TripProof> findByLrIdAndIsReviewedFalseAndIsActiveTrue(Long lrId);
}