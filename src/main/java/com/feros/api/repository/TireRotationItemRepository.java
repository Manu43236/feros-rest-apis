package com.feros.api.repository;

import com.feros.api.entity.TireRotationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TireRotationItemRepository extends JpaRepository<TireRotationItem, Long> {

    List<TireRotationItem> findByRotationLogIdOrderById(Long rotationLogId);
}
