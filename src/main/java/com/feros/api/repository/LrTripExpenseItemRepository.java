package com.feros.api.repository;

import com.feros.api.entity.LrTripExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LrTripExpenseItemRepository extends JpaRepository<LrTripExpenseItem, Long> {
    List<LrTripExpenseItem> findByTripExpenseIdAndIsActiveTrue(Long tripExpenseId);
    void deleteByTripExpenseId(Long tripExpenseId);
}
