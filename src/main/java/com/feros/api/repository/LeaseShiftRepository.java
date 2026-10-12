package com.feros.api.repository;

import com.feros.api.entity.LeaseShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaseShiftRepository extends JpaRepository<LeaseShift, Long> {

    List<LeaseShift> findByLeaseIdOrderByShiftNumberAsc(Long leaseId);

    void deleteByLeaseId(Long leaseId);
}
