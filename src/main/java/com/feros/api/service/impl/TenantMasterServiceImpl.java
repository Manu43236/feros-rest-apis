package com.feros.api.service.impl;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.RbacLoginAccessResponse;
import com.feros.api.entity.RbacLoginAccess;
import com.feros.api.enums.DeviceType;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.RbacLoginAccessRepository;
import org.springframework.transaction.annotation.Transactional;
import com.feros.api.dto.response.TenantMasterResponse;
import com.feros.api.entity.Designation;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.master.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.TenantMasterService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantMasterServiceImpl implements TenantMasterService {

    private final TenantRepository tenantRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final RouteRepository routeRepository;
    private final DesignationRepository designationRepository;
    private final PayRateRepository payRateRepository;
    private final ChargeTypeRepository chargeTypeRepository;
    private final PaymentTermsRepository paymentTermsRepository;
    private final ClientTypeRepository clientTypeRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final RbacLoginAccessRepository rbacLoginAccessRepository;
    private final CityRepository cityRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    private Tenant getCurrentTenant() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    // ===================== VEHICLE STATUSES =====================
    @Override
    public TenantMasterResponse createVehicleStatus(VehicleStatusRequest request) {
        vehicleStatusRepository.findByNameIgnoreCaseAndIsActiveTrue(request.getName())
                .ifPresent(s -> {
                    throw new FerosException("Vehicle status '" + request.getName() + "' already exists", HttpStatus.CONFLICT);
                });
        VehicleStatus status = VehicleStatus.builder()
                .name(request.getName())
                .statusType(request.getStatusType())
                .isActive(true).build();
        return mapVehicleStatus(vehicleStatusRepository.save(status));
    }

    @Override
    public TenantMasterResponse getVehicleStatusById(Long id) {
        return mapVehicleStatus(vehicleStatusRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllVehicleStatuses() {
        return vehicleStatusRepository.findByIsActiveTrue()
                .stream().map(this::mapVehicleStatus).toList();
    }

    @Override
    public TenantMasterResponse updateVehicleStatus(Long id, VehicleStatusRequest request) {
        VehicleStatus status = vehicleStatusRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND));
        status.setName(request.getName());
        if (request.getStatusType() != null) status.setStatusType(request.getStatusType());
        return mapVehicleStatus(vehicleStatusRepository.save(status));
    }

    @Override
    public void deleteVehicleStatus(Long id) {
        VehicleStatus status = vehicleStatusRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND));
        status.setIsActive(false);
        vehicleStatusRepository.save(status);
    }

    // ===================== ROUTES =====================
    @Override
    public TenantMasterResponse createRoute(RouteRequest request) {
        Tenant tenant = getCurrentTenant();
        City sourceCity = cityRepository.findById(request.getSourceCityId())
                .orElseThrow(() -> new FerosException("Source city not found", HttpStatus.NOT_FOUND));
        City destinationCity = cityRepository.findById(request.getDestinationCityId())
                .orElseThrow(() -> new FerosException("Destination city not found", HttpStatus.NOT_FOUND));
        Route route = Route.builder()
                .tenant(tenant).name(request.getName())
                .sourceCity(sourceCity).destinationCity(destinationCity)
                .distanceInKm(request.getDistanceInKm())
                .estimatedHours(request.getEstimatedHours())
                .isActive(true).build();
        return mapRoute(routeRepository.save(route));
    }

    @Override
    public TenantMasterResponse getRouteById(Long id) {
        return mapRoute(routeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Route not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllRoutes() {
        return routeRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapRoute).toList();
    }

    @Override
    public TenantMasterResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Route not found", HttpStatus.NOT_FOUND));
        City sourceCity = cityRepository.findById(request.getSourceCityId())
                .orElseThrow(() -> new FerosException("Source city not found", HttpStatus.NOT_FOUND));
        City destinationCity = cityRepository.findById(request.getDestinationCityId())
                .orElseThrow(() -> new FerosException("Destination city not found", HttpStatus.NOT_FOUND));
        route.setName(request.getName());
        route.setSourceCity(sourceCity);
        route.setDestinationCity(destinationCity);
        route.setDistanceInKm(request.getDistanceInKm());
        route.setEstimatedHours(request.getEstimatedHours());
        return mapRoute(routeRepository.save(route));
    }

    @Override
    public void deleteRoute(Long id) {
        Route route = routeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Route not found", HttpStatus.NOT_FOUND));
        route.setIsActive(false);
        routeRepository.save(route);
    }

    // ===================== DESIGNATIONS =====================
    @Override
    public TenantMasterResponse createDesignation(DesignationRequest request) {
        Tenant tenant = getCurrentTenant();
        Designation designation = Designation.builder()
                .tenant(tenant).name(request.getName())
                .roleType(request.getRoleType()).isActive(true).build();
        return mapDesignation(designationRepository.save(designation));
    }

    @Override
    public TenantMasterResponse getDesignationById(Long id) {
        return mapDesignation(designationRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Designation not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllDesignations() {
        return designationRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapDesignation).toList();
    }

    @Override
    public TenantMasterResponse updateDesignation(Long id, DesignationRequest request) {
        Designation designation = designationRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Designation not found", HttpStatus.NOT_FOUND));
        designation.setName(request.getName());
        designation.setRoleType(request.getRoleType());
        return mapDesignation(designationRepository.save(designation));
    }

    @Override
    public void deleteDesignation(Long id) {
        Designation designation = designationRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Designation not found", HttpStatus.NOT_FOUND));
        designation.setIsActive(false);
        designationRepository.save(designation);
    }

    // ===================== PAY RATES =====================
    @Override
    public TenantMasterResponse createPayRate(PayRateRequest request) {
        Tenant tenant = getCurrentTenant();
        Designation designation = designationRepository.findByIdAndTenantId(
                request.getDesignationId(), getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Designation not found", HttpStatus.NOT_FOUND));
        PayRate payRate = PayRate.builder()
                .tenant(tenant).designation(designation)
                .payPerDay(request.getPayPerDay())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isActive(true).build();
        if (request.getVehicleTypeId() != null) {
            VehicleType vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                    .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND));
            payRate.setVehicleType(vehicleType);
        }
        return mapPayRate(payRateRepository.save(payRate));
    }

    @Override
    public TenantMasterResponse getPayRateById(Long id) {
        return mapPayRate(payRateRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Pay rate not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllPayRates() {
        return payRateRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapPayRate).toList();
    }

    @Override
    public TenantMasterResponse updatePayRate(Long id, PayRateRequest request) {
        PayRate payRate = payRateRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Pay rate not found", HttpStatus.NOT_FOUND));
        Designation designation = designationRepository.findByIdAndTenantId(
                request.getDesignationId(), getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Designation not found", HttpStatus.NOT_FOUND));
        payRate.setDesignation(designation);
        payRate.setPayPerDay(request.getPayPerDay());
        payRate.setEffectiveFrom(request.getEffectiveFrom());
        payRate.setEffectiveTo(request.getEffectiveTo());
        if (request.getVehicleTypeId() != null) {
            VehicleType vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                    .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND));
            payRate.setVehicleType(vehicleType);
        }
        return mapPayRate(payRateRepository.save(payRate));
    }

    @Override
    public void deletePayRate(Long id) {
        PayRate payRate = payRateRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Pay rate not found", HttpStatus.NOT_FOUND));
        payRate.setIsActive(false);
        payRateRepository.save(payRate);
    }

    // ===================== CHARGE TYPES =====================
    @Override
    public TenantMasterResponse createChargeType(ChargeTypeRequest request) {
        Tenant tenant = getCurrentTenant();
        ChargeType chargeType = ChargeType.builder()
                .tenant(tenant).name(request.getName())
                .description(request.getDescription()).isActive(true).build();
        return mapChargeType(chargeTypeRepository.save(chargeType));
    }

    @Override
    public TenantMasterResponse getChargeTypeById(Long id) {
        return mapChargeType(chargeTypeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Charge type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllChargeTypes() {
        return chargeTypeRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapChargeType).toList();
    }

    @Override
    public TenantMasterResponse updateChargeType(Long id, ChargeTypeRequest request) {
        ChargeType chargeType = chargeTypeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Charge type not found", HttpStatus.NOT_FOUND));
        chargeType.setName(request.getName());
        chargeType.setDescription(request.getDescription());
        return mapChargeType(chargeTypeRepository.save(chargeType));
    }

    @Override
    public void deleteChargeType(Long id) {
        ChargeType chargeType = chargeTypeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Charge type not found", HttpStatus.NOT_FOUND));
        chargeType.setIsActive(false);
        chargeTypeRepository.save(chargeType);
    }

    // ===================== PAYMENT TERMS =====================
    @Override
    public TenantMasterResponse createPaymentTerms(PaymentTermsRequest request) {
        Tenant tenant = getCurrentTenant();
        PaymentTerms terms = PaymentTerms.builder()
                .tenant(tenant).name(request.getName())
                .creditDays(request.getCreditDays()).isActive(true).build();
        return mapPaymentTerms(paymentTermsRepository.save(terms));
    }

    @Override
    public TenantMasterResponse getPaymentTermsById(Long id) {
        return mapPaymentTerms(paymentTermsRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Selected payment terms are invalid or no longer available. Please select valid payment terms.", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllPaymentTerms() {
        return paymentTermsRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapPaymentTerms).toList();
    }

    @Override
    public TenantMasterResponse updatePaymentTerms(Long id, PaymentTermsRequest request) {
        PaymentTerms terms = paymentTermsRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Selected payment terms are invalid or no longer available. Please select valid payment terms.", HttpStatus.NOT_FOUND));
        terms.setName(request.getName());
        terms.setCreditDays(request.getCreditDays());
        return mapPaymentTerms(paymentTermsRepository.save(terms));
    }

    @Override
    public void deletePaymentTerms(Long id) {
        PaymentTerms terms = paymentTermsRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Selected payment terms are invalid or no longer available. Please select valid payment terms.", HttpStatus.NOT_FOUND));
        terms.setIsActive(false);
        paymentTermsRepository.save(terms);
    }

    // ===================== CLIENT TYPES =====================
    @Override
    public TenantMasterResponse createClientType(ClientTypeRequest request) {
        Tenant tenant = getCurrentTenant();
        ClientType clientType = ClientType.builder()
                .tenant(tenant).name(request.getName()).isActive(true).build();
        return mapClientType(clientTypeRepository.save(clientType));
    }

    @Override
    public TenantMasterResponse getClientTypeById(Long id) {
        return mapClientType(clientTypeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<TenantMasterResponse> getAllClientTypes() {
        return clientTypeRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapClientType).toList();
    }

    @Override
    public TenantMasterResponse updateClientType(Long id, ClientTypeRequest request) {
        ClientType clientType = clientTypeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client type not found", HttpStatus.NOT_FOUND));
        clientType.setName(request.getName());
        return mapClientType(clientTypeRepository.save(clientType));
    }

    @Override
    public void deleteClientType(Long id) {
        ClientType clientType = clientTypeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client type not found", HttpStatus.NOT_FOUND));
        clientType.setIsActive(false);
        clientTypeRepository.save(clientType);
    }

    // ===================== TENANT SETTINGS =====================
    @Override
    public TenantMasterResponse createOrUpdateSettings(TenantSettingsRequest request) {
        Tenant tenant = getCurrentTenant();
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                .orElse(TenantSettings.builder().tenant(tenant).isActive(true).build());

        if (request.getPayCycle() != null)
            settings.setPayCycle(request.getPayCycle());
        if (request.getOvertimeThresholdHours() != null)
            settings.setOvertimeThresholdHours(request.getOvertimeThresholdHours());
        if (request.getOvertimeRateMultiplier() != null)
            settings.setOvertimeRateMultiplier(request.getOvertimeRateMultiplier());
        if (request.getMaxAdvanceAmount() != null)
            settings.setMaxAdvanceAmount(request.getMaxAdvanceAmount());
        if (request.getMaxAdvanceDeductionPerCycle() != null)
            settings.setMaxAdvanceDeductionPerCycle(request.getMaxAdvanceDeductionPerCycle());
        if (request.getIsTripBonusEnabled() != null)
            settings.setIsTripBonusEnabled(request.getIsTripBonusEnabled());
        if (request.getTripBonusAmount() != null)
            settings.setTripBonusAmount(request.getTripBonusAmount());
        if (request.getAttendanceEnforced() != null)
            settings.setAttendanceEnforced(request.getAttendanceEnforced());
        if (request.getAttendanceDeadlineTime() != null)
            settings.setAttendanceDeadlineTime(request.getAttendanceDeadlineTime());
        if (request.getRequireTyreApproval() != null)
            settings.setRequireTyreApproval(request.getRequireTyreApproval());
        if (request.getRequireSparePartApproval() != null)
            settings.setRequireSparePartApproval(request.getRequireSparePartApproval());

        return mapSettings(tenantSettingsRepository.save(settings));
    }

    @Override
    public TenantMasterResponse getSettings() {
        return mapSettings(tenantSettingsRepository.findByTenantId(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Settings not configured", HttpStatus.NOT_FOUND)));
    }

    // ===================== MAPPERS =====================
    private TenantMasterResponse mapVehicleStatus(VehicleStatus s) {
        return TenantMasterResponse.builder().id(s.getId())
                .name(s.getName())
                .statusType(s.getStatusType())
                .isActive(s.getIsActive()).createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapRoute(Route r) {
        return TenantMasterResponse.builder().id(r.getId())
                .tenantId(r.getTenant().getId()).name(r.getName())
                .sourceCityId(r.getSourceCity().getId())
                .sourceCityName(r.getSourceCity().getName())
                .destinationCityId(r.getDestinationCity().getId())
                .destinationCityName(r.getDestinationCity().getName())
                .distanceInKm(r.getDistanceInKm())
                .estimatedHours(r.getEstimatedHours())
                .isActive(r.getIsActive()).createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapDesignation(Designation d) {
        return TenantMasterResponse.builder().id(d.getId())
                .tenantId(d.getTenant().getId()).name(d.getName())
                .roleType(d.getRoleType()).isActive(d.getIsActive())
                .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapPayRate(PayRate p) {
        return TenantMasterResponse.builder().id(p.getId())
                .tenantId(p.getTenant().getId())
                .designationId(p.getDesignation().getId())
                .designationName(p.getDesignation().getName())
                .vehicleTypeId(p.getVehicleType() != null ? p.getVehicleType().getId() : null)
                .vehicleTypeName(p.getVehicleType() != null ? p.getVehicleType().getName() : null)
                .payPerDay(p.getPayPerDay())
                .effectiveFrom(p.getEffectiveFrom())
                .effectiveTo(p.getEffectiveTo())
                .isActive(p.getIsActive()).createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapChargeType(ChargeType c) {
        return TenantMasterResponse.builder().id(c.getId())
                .tenantId(c.getTenant().getId()).name(c.getName())
                .description(c.getDescription()).isActive(c.getIsActive())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapPaymentTerms(PaymentTerms p) {
        return TenantMasterResponse.builder().id(p.getId())
                .tenantId(p.getTenant().getId()).name(p.getName())
                .creditDays(p.getCreditDays()).isActive(p.getIsActive())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapClientType(ClientType c) {
        return TenantMasterResponse.builder().id(c.getId())
                .tenantId(c.getTenant().getId()).name(c.getName())
                .isActive(c.getIsActive()).createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt()).build();
    }

    private TenantMasterResponse mapSettings(TenantSettings s) {
        return TenantMasterResponse.builder().id(s.getId())
                .tenantId(s.getTenant().getId())
                .payCycle(s.getPayCycle())
                .overtimeThresholdHours(s.getOvertimeThresholdHours())
                .overtimeRateMultiplier(s.getOvertimeRateMultiplier())
                .maxAdvanceAmount(s.getMaxAdvanceAmount())
                .maxAdvanceDeductionPerCycle(s.getMaxAdvanceDeductionPerCycle())
                .isTripBonusEnabled(s.getIsTripBonusEnabled())
                .tripBonusAmount(s.getTripBonusAmount())
                .attendanceEnforced(s.getAttendanceEnforced())
                .attendanceDeadlineTime(s.getAttendanceDeadlineTime())
                .requireTyreApproval(s.getRequireTyreApproval())
                .requireSparePartApproval(s.getRequireSparePartApproval())
                .isActive(s.getIsActive()).createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt()).build();
    }

    // ===================== RBAC — LOGIN ACCESS =====================

    @Override
    public RbacLoginAccessResponse getLoginAccess() {
        Long tenantId = getCurrentTenantId();
        List<RbacLoginAccess> records = rbacLoginAccessRepository.findByTenantId(tenantId);

        // Build full matrix with defaults (true) for any missing entries
        List<RbacLoginAccessResponse.Entry> entries = new java.util.ArrayList<>();
        for (RoleName role : new RoleName[]{RoleName.OFFICE_STAFF, RoleName.SUPERVISOR,
                RoleName.DRIVER, RoleName.CLEANER, RoleName.STORE_KEEPER, RoleName.SERVICE_MEN}) {
            for (DeviceType platform : DeviceType.values()) {
                boolean allowed = records.stream()
                        .filter(r -> r.getRole() == role && r.getPlatform() == platform)
                        .map(RbacLoginAccess::getAllowed)
                        .findFirst()
                        .orElse(true);
                entries.add(RbacLoginAccessResponse.Entry.builder()
                        .role(role).platform(platform).allowed(allowed).build());
            }
        }
        return RbacLoginAccessResponse.builder().entries(entries).build();
    }

    @Override
    @Transactional
    public RbacLoginAccessResponse saveLoginAccess(RbacLoginAccessRequest request) {
        Tenant tenant = getCurrentTenant();
        rbacLoginAccessRepository.deleteByTenantId(tenant.getId());

        List<RbacLoginAccess> toSave = new java.util.ArrayList<>();
        if (request.getEntries() != null) {
            for (RbacLoginAccessRequest.Entry e : request.getEntries()) {
                toSave.add(RbacLoginAccess.builder()
                        .tenant(tenant)
                        .role(e.getRole())
                        .platform(e.getPlatform())
                        .allowed(Boolean.TRUE.equals(e.getAllowed()))
                        .build());
            }
        }
        rbacLoginAccessRepository.saveAll(toSave);
        return getLoginAccess();
    }
}