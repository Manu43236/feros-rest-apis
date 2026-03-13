package com.feros.api.repository;

import com.feros.api.entity.LrCheckpost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LrCheckpostRepository extends JpaRepository<LrCheckpost, Long> {
    List<LrCheckpost> findByLrIdAndIsActiveTrue(Long lrId);
}