package com.feros.api.repository;

import com.feros.api.entity.TyreRetreadLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TyreRetreadLogRepository extends JpaRepository<TyreRetreadLog, Long> {

    List<TyreRetreadLog> findByTyreIdOrderByRetreadNumberDesc(Long tyreId);
}
