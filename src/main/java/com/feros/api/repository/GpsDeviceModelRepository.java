package com.feros.api.repository;

import com.feros.api.entity.master.GpsDeviceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GpsDeviceModelRepository extends JpaRepository<GpsDeviceModel, Long> {
    List<GpsDeviceModel> findAllByOrderByCompanyNameAscModelNameAsc();
    List<GpsDeviceModel> findAllByIsActiveTrueOrderByCompanyNameAscModelNameAsc();
    List<GpsDeviceModel> findAllByCompanyNameIgnoreCaseOrderByModelNameAsc(String companyName);
    Optional<GpsDeviceModel> findByParserKey(String parserKey);
}
