package com.feros.api.repository;

import com.feros.api.entity.TyreRotationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TyreRotationItemRepository extends JpaRepository<TyreRotationItem, Long> {

    List<TyreRotationItem> findByRotationLogIdOrderById(Long rotationLogId);
}
