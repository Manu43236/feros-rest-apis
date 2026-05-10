package com.feros.api.repository;

import com.feros.api.entity.User;
import com.feros.api.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<User> findAllByTenantId(Long tenantId);
    long countByTenantIdAndIsActiveTrue(Long tenantId);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.tenant.id = :tenantId AND u.isActive = true AND r.name IN :roles")
    List<User> findByTenantIdAndRoleNames(@Param("tenantId") Long tenantId, @Param("roles") List<RoleName> roles);
}