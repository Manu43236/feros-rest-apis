package com.feros.api.repository;

import com.feros.api.entity.UserSession;
import com.feros.api.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByUserIdAndDeviceType(Long userId, DeviceType deviceType);

    Optional<UserSession> findByToken(String token);

    void deleteByUserIdAndDeviceType(Long userId, DeviceType deviceType);
}
