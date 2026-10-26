package com.feros.api.repository;

import com.feros.api.entity.WorkOrderShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WorkOrderShiftRepository extends JpaRepository<WorkOrderShift, Long> {

    List<WorkOrderShift> findByWorkOrderIdOrderByShiftNumberAsc(Long workOrderId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WorkOrderShift s WHERE s.workOrder.id = :workOrderId")
    void deleteByWorkOrderId(Long workOrderId);
}
