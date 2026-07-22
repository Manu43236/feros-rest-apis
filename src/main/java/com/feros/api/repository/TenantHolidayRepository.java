package com.feros.api.repository;

import com.feros.api.entity.TenantHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TenantHolidayRepository extends JpaRepository<TenantHoliday, Long> {

    List<TenantHoliday> findByTenantIdAndIsActiveTrueOrderByHolidayDateAsc(Long tenantId);

    @Query("SELECT h FROM TenantHoliday h WHERE h.tenant.id = :tenantId AND YEAR(h.holidayDate) = :year AND h.isActive = true ORDER BY h.holidayDate")
    List<TenantHoliday> findByTenantIdAndYear(@Param("tenantId") Long tenantId, @Param("year") int year);

    @Query("SELECT h.holidayDate FROM TenantHoliday h WHERE h.tenant.id = :tenantId AND h.holidayDate BETWEEN :from AND :to AND h.isActive = true")
    Set<LocalDate> findHolidayDatesBetween(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    Optional<TenantHoliday> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndHolidayDateAndIsActiveTrue(Long tenantId, LocalDate holidayDate);
}
