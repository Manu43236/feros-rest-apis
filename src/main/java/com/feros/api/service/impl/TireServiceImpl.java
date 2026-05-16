package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import com.feros.api.entity.*;
import com.feros.api.enums.TireRemovalReason;
import com.feros.api.enums.TireStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.TireService;
import com.feros.api.service.NotificationService;
import com.feros.api.enums.RoleName;
import java.util.Arrays;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TireServiceImpl implements TireService {

    private final TireRepository tireRepository;
    private final VehicleTirePositionRepository positionRepository;
    private final VehicleTireFittingRepository fittingRepository;
    private final TireRotationLogRepository rotationLogRepository;
    private final TireRotationItemRepository rotationItemRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    // ── Tire CRUD ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TireResponse createTire(TireRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = getTenant(tenantId);

        tireRepository.findByTenantIdAndSerialNumberAndIsActiveTrue(tenantId, request.getSerialNumber())
                .ifPresent(t -> { throw new FerosException("Tire with this serial number already exists", HttpStatus.CONFLICT); });

        LocalDate baseDate = request.getPurchaseDate() != null ? request.getPurchaseDate() : TimeUtil.today();
        LocalDate expiryDate = request.getTyreLifeYears() != null ? baseDate.plusYears(request.getTyreLifeYears()) : null;

        Tire tire = Tire.builder()
                .tenant(tenant)
                .serialNumber(request.getSerialNumber())
                .brand(request.getBrand())
                .size(request.getSize())
                .tireType(request.getTireType())
                .plyRating(request.getPlyRating())
                .purchaseDate(request.getPurchaseDate())
                .purchaseCost(request.getPurchaseCost())
                .status(TireStatus.IN_STOCK)
                .retreadCount(0)
                .totalLifetimeKm(BigDecimal.ZERO)
                .tyreLifeYears(request.getTyreLifeYears())
                .expiryDate(expiryDate)
                .maxLifetimeKm(request.getMaxLifetimeKm())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        return toTireResponse(tireRepository.save(tire), null);
    }

    @Override
    public List<TireResponse> getAllTires() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Tire> tires = tireRepository.findByTenantIdAndIsActiveTrueOrderByIdDesc(tenantId);

        // Batch fetch all active fittings to avoid N+1
        Map<Long, VehicleTireFitting> fittingByTireId = fittingRepository
                .findAllActiveFittingsByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(f -> f.getTire().getId(), f -> f));

        return tires.stream()
                .map(t -> toTireResponse(t, fittingByTireId.get(t.getId())))
                .toList();
    }

    @Override
    public List<TireResponse> getAvailableTires() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tireRepository.findByTenantIdAndStatusAndIsActiveTrueOrderByIdDesc(tenantId, TireStatus.IN_STOCK)
                .stream().map(t -> toTireResponse(t, null)).toList();
    }

    @Override
    @Transactional
    public TireResponse updateTire(Long id, TireRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tire tire = findTire(id, tenantId);

        if (request.getBrand() != null)        tire.setBrand(request.getBrand());
        if (request.getSize() != null)         tire.setSize(request.getSize());
        if (request.getTireType() != null)     tire.setTireType(request.getTireType());
        if (request.getPlyRating() != null)    tire.setPlyRating(request.getPlyRating());
        if (request.getPurchaseDate() != null) tire.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchaseCost() != null) tire.setPurchaseCost(request.getPurchaseCost());
        if (request.getNotes() != null)        tire.setNotes(request.getNotes());
        if (request.getMaxLifetimeKm() != null)   tire.setMaxLifetimeKm(request.getMaxLifetimeKm());
        if (request.getTyreLifeYears() != null) {
            tire.setTyreLifeYears(request.getTyreLifeYears());
            LocalDate base = tire.getPurchaseDate() != null ? tire.getPurchaseDate() : TimeUtil.today();
            tire.setExpiryDate(base.plusYears(request.getTyreLifeYears()));
        }

        return toTireResponse(tireRepository.save(tire), null);
    }

    // ── Positions ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TirePositionResponse addPosition(TirePositionRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = getTenant(tenantId);
        Vehicle vehicle = getVehicle(request.getVehicleId(), tenantId);

        positionRepository.findByVehicleIdAndPositionCodeAndIsActiveTrue(vehicle.getId(), request.getPositionCode())
                .ifPresent(p -> { throw new FerosException("Position code already exists for this vehicle", HttpStatus.CONFLICT); });

        VehicleTirePosition position = VehicleTirePosition.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .positionCode(request.getPositionCode())
                .positionType(request.getPositionType())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();

        return toPositionResponse(positionRepository.save(position), null);
    }

    @Override
    public List<TirePositionResponse> getPositionsForVehicle(Long vehicleId) {
        return positionRepository.findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(vehicleId)
                .stream().map(p -> toPositionResponse(p, null)).toList();
    }

    @Override
    public List<TirePositionResponse> getCurrentPositionsForVehicle(Long vehicleId) {
        List<VehicleTirePosition> positions = positionRepository
                .findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(vehicleId);

        // Get all active fittings for this vehicle indexed by positionId
        Map<Long, VehicleTireFitting> fittingByPosition = fittingRepository
                .findByVehicleIdAndRemovedDateIsNullAndIsActiveTrueOrderByPositionDisplayOrderAsc(vehicleId)
                .stream().collect(Collectors.toMap(f -> f.getPosition().getId(), f -> f));

        return positions.stream()
                .map(p -> toPositionResponse(p, fittingByPosition.get(p.getId())))
                .toList();
    }

    @Override
    @Transactional
    public TirePositionResponse updatePosition(Long id, TirePositionRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleTirePosition position = findPosition(id, tenantId);

        if (request.getPositionCode() != null) position.setPositionCode(request.getPositionCode());
        if (request.getPositionType() != null) position.setPositionType(request.getPositionType());
        if (request.getDisplayOrder() != null) position.setDisplayOrder(request.getDisplayOrder());

        return toPositionResponse(positionRepository.save(position), null);
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleTirePosition position = findPosition(id, tenantId);

        // Cannot delete if a tire is currently fitted here
        fittingRepository.findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(id)
                .ifPresent(f -> { throw new FerosException("Cannot remove position — tire is currently fitted here", HttpStatus.CONFLICT); });

        position.setIsActive(false);
        positionRepository.save(position);
    }

    // ── Fittings ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TireFittingResponse fitTire(TireFitRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = getTenant(tenantId);
        Vehicle vehicle = getVehicle(request.getVehicleId(), tenantId);
        Tire tire = findTire(request.getTireId(), tenantId);
        VehicleTirePosition position = findPosition(request.getPositionId(), tenantId);
        User fittedBy = getUser(userId);

        // Validations
        if (tire.getStatus() != TireStatus.IN_STOCK) {
            throw new FerosException("Tire is not available (status: " + tire.getStatus() + ")", HttpStatus.CONFLICT);
        }
        fittingRepository.findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(position.getId())
                .ifPresent(f -> { throw new FerosException("Position already has a tire fitted", HttpStatus.CONFLICT); });

        VehicleTireFitting fitting = VehicleTireFitting.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .tire(tire)
                .position(position)
                .fittedAtKm(request.getFittedAtKm())
                .fittedDate(request.getFittedDate() != null ? request.getFittedDate() : TimeUtil.today())
                .fittedBy(fittedBy)
                .notes(request.getNotes())
                .isActive(true)
                .build();

        fitting = fittingRepository.save(fitting);
        tire.setStatus(TireStatus.FITTED);
        tireRepository.save(tire);

        // Notify ADMIN + OFFICE_STAFF
        notificationService.sendToRoles(tenant,
                Arrays.asList(RoleName.ADMIN, RoleName.OFFICE_STAFF),
                com.feros.api.enums.NotificationType.TYRE_FITTED,
                "Tyre Fitted",
                tire.getSerialNumber() + " fitted to " + vehicle.getRegistrationNumber() + " at position " + position.getPositionCode());

        return toFittingResponse(fitting);
    }

    @Override
    @Transactional
    public TireFittingResponse removeTire(Long fittingId, TireRemoveRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        VehicleTireFitting fitting = fittingRepository.findById(fittingId)
                .orElseThrow(() -> new FerosException("Fitting not found", HttpStatus.NOT_FOUND));

        if (!fitting.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Fitting not found", HttpStatus.NOT_FOUND);
        }
        if (fitting.getRemovedDate() != null) {
            throw new FerosException("Tire already removed from this fitting", HttpStatus.CONFLICT);
        }

        User removedBy = getUser(userId);

        fitting.setRemovedAtKm(request.getRemovedAtKm());
        fitting.setRemovedDate(request.getRemovedDate() != null ? request.getRemovedDate() : TimeUtil.today());
        fitting.setRemovalReason(request.getRemovalReason());
        fitting.setRemovedBy(removedBy);
        fitting.setNotes(request.getNotes() != null ? request.getNotes() : fitting.getNotes());

        fittingRepository.save(fitting);

        // Accumulate km on tire
        Tire tire = fitting.getTire();
        if (request.getRemovedAtKm() != null && fitting.getFittedAtKm() != null) {
            BigDecimal km = request.getRemovedAtKm().subtract(fitting.getFittedAtKm());
            if (km.compareTo(BigDecimal.ZERO) > 0) {
                tire.setTotalLifetimeKm(tire.getTotalLifetimeKm().add(km));
            }
        }

        // Update tire status based on removal reason
        TireRemovalReason reason = request.getRemovalReason();
        if (reason == TireRemovalReason.RETREAD) {
            tire.setStatus(TireStatus.RETREADING);
            tire.setRetreadCount(tire.getRetreadCount() + 1);
            tire.setRetreaderName(request.getRetreaderName());
            tire.setExpectedReturnDate(request.getExpectedReturnDate());
        } else if (reason == TireRemovalReason.SCRAP) {
            tire.setStatus(TireStatus.SCRAPPED);
        } else {
            tire.setStatus(TireStatus.IN_STOCK);
        }
        tireRepository.save(tire);

        return toFittingResponse(fitting);
    }

    @Override
    public List<TireFittingResponse> getFittingHistory(Long vehicleId) {
        return fittingRepository.findByVehicleIdAndIsActiveTrueOrderByFittedDateDescIdDesc(vehicleId)
                .stream().map(this::toFittingResponse).toList();
    }

    @Override
    public List<TireFittingResponse> getTireHistory(Long tireId) {
        return fittingRepository.findByTireIdAndIsActiveTrueOrderByFittedDateDescIdDesc(tireId)
                .stream().map(this::toFittingResponse).toList();
    }

    // ── Rotations ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TireRotationLogResponse performRotation(TireRotationRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = getTenant(tenantId);
        Vehicle vehicle = getVehicle(request.getVehicleId(), tenantId);
        User performedBy = getUser(userId);

        List<TireRotationRequest.TireRotationMoveRequest> moves = request.getMoves();
        if (moves == null || moves.isEmpty()) {
            throw new FerosException("No moves specified for rotation", HttpStatus.BAD_REQUEST);
        }

        // Conflict check: no two tires targeting the same destination position
        Set<Long> targetPositions = new HashSet<>();
        for (TireRotationRequest.TireRotationMoveRequest move : moves) {
            if (!targetPositions.add(move.getToPositionId())) {
                throw new FerosException("Two tires cannot target the same destination position", HttpStatus.CONFLICT);
            }
        }

        // Create rotation log header
        TireRotationLog rotationLog = TireRotationLog.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .rotationDate(request.getRotationDate() != null ? request.getRotationDate() : TimeUtil.today())
                .odometerKm(request.getOdometerKm())
                .performedBy(performedBy)
                .notes(request.getNotes())
                .isActive(true)
                .build();
        rotationLog = rotationLogRepository.save(rotationLog);

        List<TireRotationItem> items = new ArrayList<>();

        for (TireRotationRequest.TireRotationMoveRequest move : moves) {
            Tire tire = findTire(move.getTireId(), tenantId);
            VehicleTirePosition fromPos = findPosition(move.getFromPositionId(), tenantId);
            VehicleTirePosition toPos = findPosition(move.getToPositionId(), tenantId);

            // Find and close the old fitting
            VehicleTireFitting oldFitting = fittingRepository
                    .findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(fromPos.getId())
                    .orElseThrow(() -> new FerosException("No active fitting found for position: " + fromPos.getPositionCode(), HttpStatus.NOT_FOUND));

            oldFitting.setRemovedAtKm(request.getOdometerKm());
            oldFitting.setRemovedDate(request.getRotationDate() != null ? request.getRotationDate() : TimeUtil.today());
            oldFitting.setRemovalReason(TireRemovalReason.ROTATION);
            oldFitting.setRemovedBy(performedBy);
            oldFitting.setRotationLog(rotationLog);
            fittingRepository.save(oldFitting);

            // Accumulate km on tire
            if (oldFitting.getFittedAtKm() != null) {
                BigDecimal km = request.getOdometerKm().subtract(oldFitting.getFittedAtKm());
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    tire.setTotalLifetimeKm(tire.getTotalLifetimeKm().add(km));
                }
            }

            // Open new fitting at the target position
            VehicleTireFitting newFitting = VehicleTireFitting.builder()
                    .tenant(tenant)
                    .vehicle(vehicle)
                    .tire(tire)
                    .position(toPos)
                    .fittedAtKm(request.getOdometerKm())
                    .fittedDate(request.getRotationDate() != null ? request.getRotationDate() : TimeUtil.today())
                    .fittedBy(performedBy)
                    .rotationLog(rotationLog)
                    .isActive(true)
                    .build();
            newFitting = fittingRepository.save(newFitting);
            tireRepository.save(tire);

            TireRotationItem item = TireRotationItem.builder()
                    .rotationLog(rotationLog)
                    .tire(tire)
                    .fromPosition(fromPos)
                    .toPosition(toPos)
                    .oldFitting(oldFitting)
                    .newFitting(newFitting)
                    .build();
            items.add(rotationItemRepository.save(item));
        }

        // Notify ADMIN + SUPERVISOR
        notificationService.sendToRoles(tenant,
                Arrays.asList(RoleName.ADMIN, RoleName.SUPERVISOR),
                com.feros.api.enums.NotificationType.TYRE_ROTATION,
                "Tyre Rotation Performed",
                "Tyre rotation performed on " + vehicle.getRegistrationNumber() + " — " + items.size() + " tyre(s) rotated at " + String.format("%,.0f", request.getOdometerKm()) + " km");

        return toRotationLogResponse(rotationLog, items);
    }

    @Override
    public List<TireRotationLogResponse> getRotationHistory(Long vehicleId) {
        return rotationLogRepository.findByVehicleIdAndIsActiveTrueOrderByRotationDateDescIdDesc(vehicleId)
                .stream().map(log -> {
                    List<TireRotationItem> items = rotationItemRepository.findByRotationLogIdOrderById(log.getId());
                    return toRotationLogResponse(log, items);
                }).toList();
    }

    @Override
    @Transactional
    public TireResponse markBackToStock(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tire tire = findTire(id, tenantId);
        if (tire.getStatus() != TireStatus.RETREADING) {
            throw new FerosException("Only tires in RETREADING status can be marked back to stock", HttpStatus.CONFLICT);
        }
        tire.setStatus(TireStatus.IN_STOCK);
        tire.setRetreaderName(null);
        tire.setExpectedReturnDate(null);
        return toTireResponse(tireRepository.save(tire), null);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private Vehicle getVehicle(Long vehicleId, Long tenantId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
        if (!vehicle.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Vehicle not found", HttpStatus.NOT_FOUND);
        }
        return vehicle;
    }

    private Tire findTire(Long tireId, Long tenantId) {
        Tire tire = tireRepository.findById(tireId)
                .orElseThrow(() -> new FerosException("Tire not found", HttpStatus.NOT_FOUND));
        if (!tire.getTenant().getId().equals(tenantId) || !tire.getIsActive()) {
            throw new FerosException("Tire not found", HttpStatus.NOT_FOUND);
        }
        return tire;
    }

    private VehicleTirePosition findPosition(Long positionId, Long tenantId) {
        VehicleTirePosition position = positionRepository.findById(positionId)
                .orElseThrow(() -> new FerosException("Position not found", HttpStatus.NOT_FOUND));
        if (!position.getTenant().getId().equals(tenantId) || !position.getIsActive()) {
            throw new FerosException("Position not found", HttpStatus.NOT_FOUND);
        }
        return position;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private TireResponse toTireResponse(Tire tire, VehicleTireFitting currentFitting) {
        return TireResponse.builder()
                .id(tire.getId())
                .tenantId(tire.getTenant().getId())
                .serialNumber(tire.getSerialNumber())
                .brand(tire.getBrand())
                .size(tire.getSize())
                .tireType(tire.getTireType())
                .plyRating(tire.getPlyRating())
                .purchaseDate(tire.getPurchaseDate())
                .purchaseCost(tire.getPurchaseCost())
                .status(tire.getStatus())
                .retreadCount(tire.getRetreadCount())
                .totalLifetimeKm(tire.getTotalLifetimeKm())
                .notes(tire.getNotes())
                .tyreLifeYears(tire.getTyreLifeYears())
                .expiryDate(tire.getExpiryDate())
                .maxLifetimeKm(tire.getMaxLifetimeKm())
                .retreaderName(tire.getRetreaderName())
                .expectedReturnDate(tire.getExpectedReturnDate())
                .currentFittingId(currentFitting != null ? currentFitting.getId() : null)
                .currentVehicleRegistrationNumber(currentFitting != null ? currentFitting.getVehicle().getRegistrationNumber() : null)
                .currentPositionCode(currentFitting != null ? currentFitting.getPosition().getPositionCode() : null)
                .createdAt(tire.getCreatedAt())
                .updatedAt(tire.getUpdatedAt())
                .build();
    }

    private TirePositionResponse toPositionResponse(VehicleTirePosition position, VehicleTireFitting currentFitting) {
        return TirePositionResponse.builder()
                .id(position.getId())
                .tenantId(position.getTenant().getId())
                .vehicleId(position.getVehicle().getId())
                .vehicleRegistrationNumber(position.getVehicle().getRegistrationNumber())
                .positionCode(position.getPositionCode())
                .positionType(position.getPositionType())
                .displayOrder(position.getDisplayOrder())
                .currentFitting(currentFitting != null ? toFittingResponse(currentFitting) : null)
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    private TireFittingResponse toFittingResponse(VehicleTireFitting f) {
        return TireFittingResponse.builder()
                .id(f.getId())
                .tenantId(f.getTenant().getId())
                .vehicleId(f.getVehicle().getId())
                .vehicleRegistrationNumber(f.getVehicle().getRegistrationNumber())
                .tireId(f.getTire().getId())
                .tireSerialNumber(f.getTire().getSerialNumber())
                .tireBrand(f.getTire().getBrand())
                .tireSize(f.getTire().getSize())
                .tireMaxLifetimeKm(f.getTire().getMaxLifetimeKm())
                .tireTotalLifetimeKm(f.getTire().getTotalLifetimeKm())
                .positionId(f.getPosition().getId())
                .positionCode(f.getPosition().getPositionCode())
                .fittedAtKm(f.getFittedAtKm())
                .fittedDate(f.getFittedDate())
                .fittedById(f.getFittedBy().getId())
                .fittedByName(f.getFittedBy().getName())
                .removedAtKm(f.getRemovedAtKm())
                .removedDate(f.getRemovedDate())
                .removalReason(f.getRemovalReason())
                .removedById(f.getRemovedBy() != null ? f.getRemovedBy().getId() : null)
                .removedByName(f.getRemovedBy() != null ? f.getRemovedBy().getName() : null)
                .rotationLogId(f.getRotationLog() != null ? f.getRotationLog().getId() : null)
                .kmDriven(f.getKmDriven())
                .notes(f.getNotes())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private TireRotationLogResponse toRotationLogResponse(TireRotationLog log, List<TireRotationItem> items) {
        return TireRotationLogResponse.builder()
                .id(log.getId())
                .tenantId(log.getTenant().getId())
                .vehicleId(log.getVehicle().getId())
                .vehicleRegistrationNumber(log.getVehicle().getRegistrationNumber())
                .rotationDate(log.getRotationDate())
                .odometerKm(log.getOdometerKm())
                .performedById(log.getPerformedBy().getId())
                .performedByName(log.getPerformedBy().getName())
                .notes(log.getNotes())
                .items(items.stream().map(this::toRotationItemResponse).toList())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }

    private TireRotationItemResponse toRotationItemResponse(TireRotationItem item) {
        return TireRotationItemResponse.builder()
                .id(item.getId())
                .tireId(item.getTire().getId())
                .tireSerialNumber(item.getTire().getSerialNumber())
                .fromPositionId(item.getFromPosition().getId())
                .fromPositionCode(item.getFromPosition().getPositionCode())
                .toPositionId(item.getToPosition().getId())
                .toPositionCode(item.getToPosition().getPositionCode())
                .oldFittingId(item.getOldFitting().getId())
                .newFittingId(item.getNewFitting().getId())
                .build();
    }
}
