package com.feros.api.repository;

import com.feros.api.entity.LrTripExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LrTripExpenseItemRepository extends JpaRepository<LrTripExpenseItem, Long> {
    List<LrTripExpenseItem> findByTripExpenseIdAndIsActiveTrue(Long tripExpenseId);
    void deleteByTripExpenseId(Long tripExpenseId);

    @Query("SELECT i FROM LrTripExpenseItem i WHERE i.tripExpense.id IN :ids AND i.isActive = true")
    List<LrTripExpenseItem> findByTripExpenseIdsAndIsActiveTrue(@Param("ids") List<Long> ids);
}
