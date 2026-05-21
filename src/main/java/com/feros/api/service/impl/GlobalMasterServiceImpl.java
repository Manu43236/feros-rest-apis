package com.feros.api.service.impl;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import com.feros.api.entity.master.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.GlobalMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalMasterServiceImpl implements GlobalMasterService {

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final FuelTypeRepository fuelTypeRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final AttendanceTypeRepository attendanceTypeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final OwnershipTypeRepository ownershipTypeRepository;
    private final TaxRepository taxRepository;
    private final DeductionTypeRepository deductionTypeRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final ServiceTaskTypeRepository serviceTaskTypeRepository;

    // ===================== STATES =====================
    @Override
    public StateResponse createState(StateRequest request) {
        State state = State.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .isActive(true)
                .build();
        return mapToStateResponse(stateRepository.save(state));
    }

    @Override
    public StateResponse getStateById(Long id) {
        return mapToStateResponse(stateRepository.findById(id)
                .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<StateResponse> getAllStates() {
        return stateRepository.findAllByIsActiveTrueOrderByNameAsc()
                .stream().map(this::mapToStateResponse).toList();
    }

    @Override
    public Page<StateResponse> getStatesPaged(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.feros.api.entity.master.State> p = (search != null && !search.isBlank())
                ? stateRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(search, pageable)
                : stateRepository.findByIsActiveTrueOrderByNameAsc(pageable);
        return p.map(this::mapToStateResponse);
    }

    @Override
    public StateResponse updateState(Long id, StateRequest request) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND));
        state.setName(request.getName());
        state.setCode(request.getCode().toUpperCase());
        return mapToStateResponse(stateRepository.save(state));
    }

    @Override
    public void deleteState(Long id) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND));
        state.setIsActive(false);
        stateRepository.save(state);
    }

    // ===================== CITIES =====================
    @Override
    public CityResponse createCity(CityRequest request) {
        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND));
        City city = City.builder().state(state).name(request.getName()).isActive(true).build();
        return mapToCityResponse(cityRepository.save(city));
    }

    @Override
    public CityResponse getCityById(Long id) {
        return mapToCityResponse(cityRepository.findById(id)
                .orElseThrow(() -> new FerosException("City not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream().map(this::mapToCityResponse).toList();
    }

    @Override
    public Page<CityResponse> getCitiesPaged(int page, int size, String search, Long stateId) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasSearch = search != null && !search.isBlank();
        Page<com.feros.api.entity.master.City> p;
        if (stateId != null && hasSearch) {
            p = cityRepository.findByStateIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(stateId, search, pageable);
        } else if (stateId != null) {
            p = cityRepository.findByStateIdAndIsActiveTrueOrderByNameAsc(stateId, pageable);
        } else if (hasSearch) {
            p = cityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(search, pageable);
        } else {
            p = cityRepository.findByIsActiveTrueOrderByNameAsc(pageable);
        }
        return p.map(this::mapToCityResponse);
    }

    @Override
    public List<CityResponse> getCitiesByState(Long stateId) {
        return cityRepository.findByStateIdAndIsActiveTrueOrderByNameAsc(stateId)
                .stream().map(this::mapToCityResponse).toList();
    }

    @Override
    public CityResponse updateCity(Long id, CityRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new FerosException("City not found", HttpStatus.NOT_FOUND));
        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND));
        city.setName(request.getName());
        city.setState(state);
        return mapToCityResponse(cityRepository.save(city));
    }

    @Override
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new FerosException("City not found", HttpStatus.NOT_FOUND));
        city.setIsActive(false);
        cityRepository.save(city);
    }

    // ===================== VEHICLE BRANDS =====================
    @Override
    public MasterResponse createVehicleBrand(MasterRequest request) {
        VehicleBrand brand = VehicleBrand.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(vehicleBrandRepository.save(brand));
    }

    @Override
    public MasterResponse getVehicleBrandById(Long id) {
        return mapToMasterResponse(vehicleBrandRepository.findById(id)
                .orElseThrow(() -> new FerosException("Vehicle brand not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllVehicleBrands() {
        return vehicleBrandRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateVehicleBrand(Long id, MasterRequest request) {
        VehicleBrand brand = vehicleBrandRepository.findById(id)
                .orElseThrow(() -> new FerosException("Vehicle brand not found", HttpStatus.NOT_FOUND));
        brand.setName(request.getName());
        return mapToMasterResponse(vehicleBrandRepository.save(brand));
    }

    @Override
    public void deleteVehicleBrand(Long id) {
        VehicleBrand brand = vehicleBrandRepository.findById(id)
                .orElseThrow(() -> new FerosException("Vehicle brand not found", HttpStatus.NOT_FOUND));
        brand.setIsActive(false);
        vehicleBrandRepository.save(brand);
    }

    // ===================== VEHICLE TYPES =====================
    @Override
    public MasterResponse createVehicleType(VehicleTypeRequest request) {
        VehicleType type = VehicleType.builder()
                .name(request.getName())
                .capacityInTons(request.getCapacityInTons())
                .tyreCount(request.getTyreCount())
                .isActive(true).build();
        return mapToMasterResponse(vehicleTypeRepository.save(type));
    }

    @Override
    public MasterResponse getVehicleTypeById(Long id) {
        return mapToMasterResponse(vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllVehicleTypes() {
        return vehicleTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateVehicleType(Long id, VehicleTypeRequest request) {
        VehicleType type = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        type.setCapacityInTons(request.getCapacityInTons());
        type.setTyreCount(request.getTyreCount());
        return mapToMasterResponse(vehicleTypeRepository.save(type));
    }

    @Override
    public void deleteVehicleType(Long id) {
        VehicleType type = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        vehicleTypeRepository.save(type);
    }

    // ===================== FUEL TYPES =====================
    @Override
    public MasterResponse createFuelType(MasterRequest request) {
        FuelType type = FuelType.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(fuelTypeRepository.save(type));
    }

    @Override
    public MasterResponse getFuelTypeById(Long id) {
        return mapToMasterResponse(fuelTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Fuel type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllFuelTypes() {
        return fuelTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateFuelType(Long id, MasterRequest request) {
        FuelType type = fuelTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Fuel type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        return mapToMasterResponse(fuelTypeRepository.save(type));
    }

    @Override
    public void deleteFuelType(Long id) {
        FuelType type = fuelTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Fuel type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        fuelTypeRepository.save(type);
    }

    // ===================== MATERIAL TYPES =====================
    @Override
    public MasterResponse createMaterialType(MaterialTypeRequest request) {
        MaterialType type = MaterialType.builder()
                .name(request.getName()).category(request.getCategory()).isActive(true).build();
        return mapToMasterResponse(materialTypeRepository.save(type));
    }

    @Override
    public MasterResponse getMaterialTypeById(Long id) {
        return mapToMasterResponse(materialTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Material type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllMaterialTypes() {
        return materialTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateMaterialType(Long id, MaterialTypeRequest request) {
        MaterialType type = materialTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Material type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        type.setCategory(request.getCategory());
        return mapToMasterResponse(materialTypeRepository.save(type));
    }

    @Override
    public void deleteMaterialType(Long id) {
        MaterialType type = materialTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Material type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        materialTypeRepository.save(type);
    }

    // ===================== DOCUMENT TYPES =====================
    @Override
    public MasterResponse createDocumentType(DocumentTypeRequest request) {
        DocumentType type = DocumentType.builder()
                .name(request.getName()).applicableFor(request.getApplicableFor())
                .applicableRoles(toRolesJson(request.getApplicableRoles())).isActive(true).build();
        return mapToMasterResponse(documentTypeRepository.save(type));
    }

    @Override
    public MasterResponse getDocumentTypeById(Long id) {
        return mapToMasterResponse(documentTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Document type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllDocumentTypes() {
        return documentTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateDocumentType(Long id, DocumentTypeRequest request) {
        DocumentType type = documentTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Document type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        type.setApplicableFor(request.getApplicableFor());
        type.setApplicableRoles(toRolesJson(request.getApplicableRoles()));
        return mapToMasterResponse(documentTypeRepository.save(type));
    }

    @Override
    public void deleteDocumentType(Long id) {
        DocumentType type = documentTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Document type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        documentTypeRepository.save(type);
    }

    // ===================== ATTENDANCE TYPES =====================
    @Override
    public MasterResponse createAttendanceType(MasterRequest request) {
        AttendanceType type = AttendanceType.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(attendanceTypeRepository.save(type));
    }

    @Override
    public MasterResponse getAttendanceTypeById(Long id) {
        return mapToMasterResponse(attendanceTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Attendance type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllAttendanceTypes() {
        return attendanceTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateAttendanceType(Long id, MasterRequest request) {
        AttendanceType type = attendanceTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Attendance type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        return mapToMasterResponse(attendanceTypeRepository.save(type));
    }

    @Override
    public void deleteAttendanceType(Long id) {
        AttendanceType type = attendanceTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Attendance type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        attendanceTypeRepository.save(type);
    }

    // ===================== LEAVE TYPES =====================
    @Override
    public MasterResponse createLeaveType(MasterRequest request) {
        LeaveType type = LeaveType.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(leaveTypeRepository.save(type));
    }

    @Override
    public MasterResponse getLeaveTypeById(Long id) {
        return mapToMasterResponse(leaveTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Leave type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateLeaveType(Long id, MasterRequest request) {
        LeaveType type = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Leave type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        return mapToMasterResponse(leaveTypeRepository.save(type));
    }

    @Override
    public void deleteLeaveType(Long id) {
        LeaveType type = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Leave type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        leaveTypeRepository.save(type);
    }

    // ===================== EMPLOYMENT TYPES =====================
    @Override
    public MasterResponse createEmploymentType(MasterRequest request) {
        EmploymentType type = EmploymentType.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(employmentTypeRepository.save(type));
    }

    @Override
    public MasterResponse getEmploymentTypeById(Long id) {
        return mapToMasterResponse(employmentTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Employment type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllEmploymentTypes() {
        return employmentTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateEmploymentType(Long id, MasterRequest request) {
        EmploymentType type = employmentTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Employment type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        return mapToMasterResponse(employmentTypeRepository.save(type));
    }

    @Override
    public void deleteEmploymentType(Long id) {
        EmploymentType type = employmentTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Employment type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        employmentTypeRepository.save(type);
    }

    // ===================== OWNERSHIP TYPES =====================
    @Override
    public MasterResponse createOwnershipType(MasterRequest request) {
        OwnershipType type = OwnershipType.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(ownershipTypeRepository.save(type));
    }

    @Override
    public MasterResponse getOwnershipTypeById(Long id) {
        return mapToMasterResponse(ownershipTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Ownership type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllOwnershipTypes() {
        return ownershipTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateOwnershipType(Long id, MasterRequest request) {
        OwnershipType type = ownershipTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Ownership type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        return mapToMasterResponse(ownershipTypeRepository.save(type));
    }

    @Override
    public void deleteOwnershipType(Long id) {
        OwnershipType type = ownershipTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Ownership type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        ownershipTypeRepository.save(type);
    }

    // ===================== TAXES =====================
    @Override
    public MasterResponse createTax(TaxRequest request) {
        Tax tax = Tax.builder()
                .name(request.getName()).rate(request.getRate())
                .taxType(request.getTaxType()).isActive(true).build();
        return mapToMasterResponse(taxRepository.save(tax));
    }

    @Override
    public MasterResponse getTaxById(Long id) {
        return mapToMasterResponse(taxRepository.findById(id)
                .orElseThrow(() -> new FerosException("Tax not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllTaxes() {
        return taxRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateTax(Long id, TaxRequest request) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new FerosException("Tax not found", HttpStatus.NOT_FOUND));
        tax.setName(request.getName());
        tax.setRate(request.getRate());
        tax.setTaxType(request.getTaxType());
        return mapToMasterResponse(taxRepository.save(tax));
    }

    @Override
    public void deleteTax(Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new FerosException("Tax not found", HttpStatus.NOT_FOUND));
        tax.setIsActive(false);
        taxRepository.save(tax);
    }

    // ===================== DEDUCTION TYPES =====================
    @Override
    public MasterResponse createDeductionType(MasterRequest request) {
        DeductionType type = DeductionType.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(deductionTypeRepository.save(type));
    }

    @Override
    public MasterResponse getDeductionTypeById(Long id) {
        return mapToMasterResponse(deductionTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Deduction type not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllDeductionTypes() {
        return deductionTypeRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updateDeductionType(Long id, MasterRequest request) {
        DeductionType type = deductionTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Deduction type not found", HttpStatus.NOT_FOUND));
        type.setName(request.getName());
        return mapToMasterResponse(deductionTypeRepository.save(type));
    }

    @Override
    public void deleteDeductionType(Long id) {
        DeductionType type = deductionTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Deduction type not found", HttpStatus.NOT_FOUND));
        type.setIsActive(false);
        deductionTypeRepository.save(type);
    }

    // ===================== PAYMENT STATUSES =====================
    @Override
    public MasterResponse createPaymentStatus(MasterRequest request) {
        PaymentStatus status = PaymentStatus.builder().name(request.getName()).isActive(true).build();
        return mapToMasterResponse(paymentStatusRepository.save(status));
    }

    @Override
    public MasterResponse getPaymentStatusById(Long id) {
        return mapToMasterResponse(paymentStatusRepository.findById(id)
                .orElseThrow(() -> new FerosException("Payment status not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<MasterResponse> getAllPaymentStatuses() {
        return paymentStatusRepository.findAllByIsActiveTrue()
                .stream().map(this::mapToMasterResponse).toList();
    }

    @Override
    public MasterResponse updatePaymentStatus(Long id, MasterRequest request) {
        PaymentStatus status = paymentStatusRepository.findById(id)
                .orElseThrow(() -> new FerosException("Payment status not found", HttpStatus.NOT_FOUND));
        status.setName(request.getName());
        return mapToMasterResponse(paymentStatusRepository.save(status));
    }

    @Override
    public void deletePaymentStatus(Long id) {
        PaymentStatus status = paymentStatusRepository.findById(id)
                .orElseThrow(() -> new FerosException("Payment status not found", HttpStatus.NOT_FOUND));
        status.setIsActive(false);
        paymentStatusRepository.save(status);
    }

    // ===================== MAPPERS =====================
    private StateResponse mapToStateResponse(State state) {
        return StateResponse.builder()
                .id(state.getId()).name(state.getName()).code(state.getCode())
                .isActive(state.getIsActive()).createdAt(state.getCreatedAt())
                .updatedAt(state.getUpdatedAt()).build();
    }

    private CityResponse mapToCityResponse(City city) {
        return CityResponse.builder()
                .id(city.getId()).stateId(city.getState().getId())
                .stateName(city.getState().getName()).name(city.getName())
                .isActive(city.getIsActive()).createdAt(city.getCreatedAt())
                .updatedAt(city.getUpdatedAt()).build();
    }

    private MasterResponse mapToMasterResponse(Object entity) {
        if (entity instanceof VehicleBrand e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof VehicleType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .capacityInTons(e.getCapacityInTons()).tyreCount(e.getTyreCount())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof FuelType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof MaterialType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .category(e.getCategory()).isActive(e.getIsActive())
                    .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof DocumentType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .applicableFor(e.getApplicableFor().name())
                    .applicableRoles(parseRoles(e.getApplicableRoles()))
                    .isActive(e.getIsActive())
                    .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof AttendanceType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof LeaveType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof EmploymentType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof OwnershipType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof Tax e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .rate(e.getRate()).taxType(e.getTaxType().name())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof DeductionType e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        if (entity instanceof PaymentStatus e)
            return MasterResponse.builder().id(e.getId()).name(e.getName())
                    .isActive(e.getIsActive()).createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt()).build();
        throw new FerosException("Unknown entity type", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== SERVICE TASK TYPES =====================
    @Override
    public MasterResponse createServiceTaskType(MasterRequest request) {
        ServiceTaskType t = ServiceTaskType.builder().name(request.getName()).isActive(true).build();
        ServiceTaskType saved = serviceTaskTypeRepository.save(t);
        return MasterResponse.builder().id(saved.getId()).name(saved.getName()).isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt()).updatedAt(saved.getUpdatedAt()).build();
    }

    @Override
    public List<MasterResponse> getAllServiceTaskTypes() {
        return serviceTaskTypeRepository.findAllByIsActiveTrueOrderByNameAsc()
                .stream().map(t -> MasterResponse.builder().id(t.getId()).name(t.getName())
                        .isActive(t.getIsActive()).createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build())
                .toList();
    }

    @Override
    public MasterResponse updateServiceTaskType(Long id, MasterRequest request) {
        ServiceTaskType t = serviceTaskTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Service task type not found", HttpStatus.NOT_FOUND));
        t.setName(request.getName());
        serviceTaskTypeRepository.save(t);
        return MasterResponse.builder().id(t.getId()).name(t.getName()).isActive(t.getIsActive())
                .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();
    }

    @Override
    public void deleteServiceTaskType(Long id) {
        ServiceTaskType t = serviceTaskTypeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Service task type not found", HttpStatus.NOT_FOUND));
        t.setIsActive(false);
        serviceTaskTypeRepository.save(t);
    }

    private List<String> parseRoles(String json) {
        if (json == null || json.isBlank()) return null;
        String stripped = json.replaceAll("[\\[\\]\"\\s]", "");
        if (stripped.isEmpty()) return List.of();
        return List.of(stripped.split(","));
    }

    private String toRolesJson(List<String> roles) {
        if (roles == null || roles.isEmpty()) return null;
        return "[" + roles.stream().map(r -> "\"" + r + "\"").collect(Collectors.joining(",")) + "]";
    }
}