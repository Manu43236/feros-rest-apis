package com.feros.api.repository;

import com.feros.api.entity.DocumentSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DocumentSequence s WHERE s.tenantId = :tenantId AND s.docType = :docType AND s.period = :period")
    Optional<DocumentSequence> findForUpdate(@Param("tenantId") Long tenantId,
                                             @Param("docType") String docType,
                                             @Param("period") String period);
}
