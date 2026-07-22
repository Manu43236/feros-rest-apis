package com.feros.api.service;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.HolidayResponse;
import com.feros.api.dto.response.RbacLoginAccessResponse;
import com.feros.api.dto.response.TenantMasterResponse;
import java.util.List;

public interface TenantMasterService {

    // Vehicle Statuses
    TenantMasterResponse createVehicleStatus(VehicleStatusRequest request);
    TenantMasterResponse getVehicleStatusById(Long id);
    List<TenantMasterResponse> getAllVehicleStatuses();
    TenantMasterResponse updateVehicleStatus(Long id, VehicleStatusRequest request);
    void deleteVehicleStatus(Long id);

    // Routes
    TenantMasterResponse createRoute(RouteRequest request);
    TenantMasterResponse getRouteById(Long id);
    List<TenantMasterResponse> getAllRoutes();
    TenantMasterResponse updateRoute(Long id, RouteRequest request);
    void deleteRoute(Long id);

    // Designations
    TenantMasterResponse createDesignation(DesignationRequest request);
    TenantMasterResponse getDesignationById(Long id);
    List<TenantMasterResponse> getAllDesignations();
    TenantMasterResponse updateDesignation(Long id, DesignationRequest request);
    void deleteDesignation(Long id);

    // Charge Types
    TenantMasterResponse createChargeType(ChargeTypeRequest request);
    TenantMasterResponse getChargeTypeById(Long id);
    List<TenantMasterResponse> getAllChargeTypes();
    TenantMasterResponse updateChargeType(Long id, ChargeTypeRequest request);
    void deleteChargeType(Long id);

    // Payment Terms
    TenantMasterResponse createPaymentTerms(PaymentTermsRequest request);
    TenantMasterResponse getPaymentTermsById(Long id);
    List<TenantMasterResponse> getAllPaymentTerms();
    TenantMasterResponse updatePaymentTerms(Long id, PaymentTermsRequest request);
    void deletePaymentTerms(Long id);

    // Client Types
    TenantMasterResponse createClientType(ClientTypeRequest request);
    TenantMasterResponse getClientTypeById(Long id);
    List<TenantMasterResponse> getAllClientTypes();
    TenantMasterResponse updateClientType(Long id, ClientTypeRequest request);
    void deleteClientType(Long id);

    // Tenant Settings
    TenantMasterResponse createOrUpdateSettings(TenantSettingsRequest request);
    TenantMasterResponse getSettings();

    // Holidays
    HolidayResponse createHoliday(HolidayRequest request);
    List<HolidayResponse> getHolidays(Integer year);
    HolidayResponse updateHoliday(Long id, HolidayRequest request);
    void deleteHoliday(Long id);

    // RBAC — Login Access
    RbacLoginAccessResponse getLoginAccess();
    RbacLoginAccessResponse saveLoginAccess(RbacLoginAccessRequest request);
}
