package com.feros.api.repository;

import com.feros.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<User> findByIdAndIsActiveTrue(Long id);
    List<User> findAllByIsActiveTrue();
    List<User> findAllByTenantIdAndIsActiveTrue(Long tenantId);
}