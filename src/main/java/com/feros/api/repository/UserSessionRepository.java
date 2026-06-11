package com.feros.api.repository;

import com.feros.api.entity.UserSession;
import com.feros.api.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByToken(String token);

    Optional<UserSession> findByUserIdAndDeviceType(Long userId, DeviceType deviceType);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId AND s.deviceType = :deviceType")
    void deleteAllByUserIdAndDeviceType(@Param("userId") Long userId, @Param("deviceType") DeviceType deviceType);
}
