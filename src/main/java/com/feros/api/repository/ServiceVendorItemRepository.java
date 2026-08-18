package com.feros.api.repository;

import com.feros.api.entity.ServiceVendorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceVendorItemRepository extends JpaRepository<ServiceVendorItem, Long> {
    List<ServiceVendorItem> findByServiceIdOrderByIdAsc(Long serviceId);
}
