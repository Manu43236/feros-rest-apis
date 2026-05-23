package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import com.feros.api.entity.*;
import com.feros.api.enums.TyreRemovalReason;
import com.feros.api.enums.TyreStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.TyreService;
import com.feros.api.service.NotificationService;
import com.feros.api.entity.master.TenantSettings;
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
public class TyreServiceImpl implements TyreService {

    private final TyreRepository tyreRepository;
    private final VehicleTyrePositionRepository positionRepository;
    private final VehicleTyreFittingRepository fittingRepository;
    private final TyreRotationLogRepository rotationLogRepository;
    private final TyreRotationItemRepository rotationItemRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantSettingsRepository tenantSettingsRepository;

    // ── Tyre CRUD ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TyreResponse createTyre(TyreRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = getTenant(tenantId);

        tyreRepository.findByTenantIdAndSerialNumberAndIsActiveTrue(tenantId, request.getSerialNumber())
                .ifPresent(t -> { throw new FerosException("Tyre with this serial number already exists", HttpStatus.CONFLICT); });

        LocalDate baseDate = request.getPurchaseDate() != null ? request.getPurchaseDate() : TimeUtil.today();
        LocalDate expiryDate = request.getTyreLifeYears() != null ? baseDate.plusYears(request.getTyreLifeYears()) : null;

        Tyre tyre = Tyre.builder()
                .tenant(tenant)
                .serialNumber(request.getSerialNumber())
                .brand(request.getBrand())
                .size(request.getSize())
                .tyreType(request.getTyreType())
                .plyRating(request.getPlyRating())
                .purchaseDate(request.getPurchaseDate())
                .purchaseCost(request.getPurchaseCost())
                .status(TyreStatus.IN_STOCK)
                .retreadCount(0)
                .totalLifetimeKm(BigDecimal.ZERO)
                .tyreLifeYears(request.getTyreLifeYears())
                .expiryDate(expiryDate)
                .maxLifetimeKm(request.getMaxLifetimeKm())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        return toTyreResponse(tyreRepository.save(tyre), null);
    }

    @Override
    public List<TyreResponse> getAllTyres() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Tyre> tyres = tyreRepository.findByTenantIdAndIsActiveTrueOrderByIdDesc(tenantId);

        // Batch fetch all active fittings to avoid N+1
        Map<Long, VehicleTyreFitting> fittingByTyreId = fittingRepository
                .findAllActiveFittingsByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(f -> f.getTyre().getId(), f -> f));

        return tyres.stream()
                .map(t -> toTyreResponse(t, fittingByTyreId.get(t.getId())))
                .toList();
    }

    @Override
    public List<TyreResponse> getAvailableTyres() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tyreRepository.findByTenantIdAndStatusAndIsActiveTrueOrderByIdDesc(tenantId, TyreStatus.IN_STOCK)
                .stream().map(t -> toTyreResponse(t, null)).toList();
    }

    @Override
    @Transactional
    public TyreResponse updateTyre(Long id, TyreRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tyre tyre = findTyre(id, tenantId);

        if (request.getBrand() != null)        tyre.setBrand(request.getBrand());
        if (request.getSize() != null)         tyre.setSize(request.getSize());
        if (request.getTyreType() != null)     tyre.setTyreType(request.getTyreType());
        if (request.getPlyRating() != null)    tyre.setPlyRating(request.getPlyRating());
        if (request.getPurchaseDate() != null) tyre.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchaseCost() != null) tyre.setPurchaseCost(request.getPurchaseCost());
        if (request.getNotes() != null)        tyre.setNotes(request.getNotes());
        if (request.getMaxLifetimeKm() != null)   tyre.setMaxLifetimeKm(request.getMaxLifetimeKm());
        if (request.getTyreLifeYears() != null) {
            tyre.setTyreLifeYears(request.getTyreLifeYears());
            LocalDate base = tyre.getPurchaseDate() != null ? tyre.getPurchaseDate() : TimeUtil.today();
            tyre.setExpiryDate(base.plusYears(request.getTyreLifeYears()));
        }

        return toTyreResponse(tyreRepository.save(tyre), null);
    }

    // ── Positions ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TyrePositionResponse addPosition(TyrePositionRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = getTenant(tenantId);
        Vehicle vehicle = getVehicle(request.getVehicleId(), tenantId);

        positionRepository.findByVehicleIdAndPositionCodeAndIsActiveTrue(vehicle.getId(), request.getPositionCode())
                .ifPresent(p -> { throw new FerosException("Position code already exists for this vehicle", HttpStatus.CONFLICT); });

        VehicleTyrePosition position = VehicleTyrePosition.builder()
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
    public List<TyrePositionResponse> getPositionsForVehicle(Long vehicleId) {
        return positionRepository.findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(vehicleId)
                .stream().map(p -> toPositionResponse(p, null)).toList();
    }

    @Override
    public List<TyrePositionResponse> getCurrentPositionsForVehicle(Long vehicleId) {
        List<VehicleTyrePosition> positions = positionRepository
                .findByVehicleIdAndIsActiveTrueOrderByDisplayOrderAsc(vehicleId);

        // Get all active fittings for this vehicle indexed by positionId
        Map<Long, VehicleTyreFitting> fittingByPosition = fittingRepository
                .findByVehicleIdAndRemovedDateIsNullAndIsActiveTrueOrderByPositionDisplayOrderAsc(vehicleId)
                .stream().collect(Collectors.toMap(f -> f.getPosition().getId(), f -> f));

        return positions.stream()
                .map(p -> toPositionResponse(p, fittingByPosition.get(p.getId())))
                .toList();
    }

    @Override
    @Transactional
    public TyrePositionResponse updatePosition(Long id, TyrePositionRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleTyrePosition position = findPosition(id, tenantId);

        if (request.getPositionCode() != null) position.setPositionCode(request.getPositionCode());
        if (request.getPositionType() != null) position.setPositionType(request.getPositionType());
        if (request.getDisplayOrder() != null) position.setDisplayOrder(request.getDisplayOrder());

        return toPositionResponse(positionRepository.save(position), null);
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleTyrePosition position = findPosition(id, tenantId);

        // Cannot delete if a tyre is currently fitted here
        fittingRepository.findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(id)
                .ifPresent(f -> { throw new FerosException("Cannot remove position — tyre is currently fitted here", HttpStatus.CONFLICT); });

        position.setIsActive(false);
        positionRepository.save(position);
    }

    // ── Fittings ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TyreFittingResponse fitTyre(TyreFitRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = getTenant(tenantId);
        Vehicle vehicle = getVehicle(request.getVehicleId(), tenantId);
        Tyre tyre = findTyre(request.getTyreId(), tenantId);
        VehicleTyrePosition position = findPosition(request.getPositionId(), tenantId);
        User fittedBy = getUser(userId);

        // Check tenant setting — SERVICE_MEN must go through STORE_KEEPER if approval required
        String currentRole = SecurityUtil.getCurrentRole();
        if ("SERVICE_MEN".equals(currentRole)) {
            TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId).orElse(null);
            if (settings != null && Boolean.TRUE.equals(settings.getRequireTyreApproval())) {
                throw new FerosException(
                        "Tyre fitting requires Store Keeper approval. Please submit a tyre request.", HttpStatus.FORBIDDEN);
            }
        }

        // Validations
        if (tyre.getStatus() != TyreStatus.IN_STOCK) {
            throw new FerosException("Tyre is not available (status: " + tyre.getStatus() + ")", HttpStatus.CONFLICT);
        }
        fittingRepository.findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(position.getId())
                .ifPresent(f -> { throw new FerosException("Position already has a tyre fitted", HttpStatus.CONFLICT); });

        VehicleTyreFitting fitting = VehicleTyreFitting.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .tyre(tyre)
                .position(position)
                .fittedAtKm(request.getFittedAtKm() != null ? request.getFittedAtKm() : java.math.BigDecimal.ZERO)
                .fittedDate(request.getFittedDate() != null ? request.getFittedDate() : TimeUtil.today())
                .fittedBy(fittedBy)
                .notes(request.getNotes())
                .isActive(true)
                .build();

        fitting = fittingRepository.save(fitting);
        tyre.setStatus(TyreStatus.FITTED);
        tyreRepository.save(tyre);

        // Notify ADMIN + OFFICE_STAFF
        notificationService.sendToRoles(tenant,
                Arrays.asList(RoleName.ADMIN, RoleName.OFFICE_STAFF),
                com.feros.api.enums.NotificationType.TYRE_FITTED,
                "Tyre Fitted",
                tyre.getSerialNumber() + " fitted to " + vehicle.getRegistrationNumber() + " at position " + position.getPositionCode());

        return toFittingResponse(fitting);
    }

    @Override
    @Transactional
    public TyreFittingResponse removeTyre(Long fittingId, TyreRemoveRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        VehicleTyreFitting fitting = fittingRepository.findById(fittingId)
                .orElseThrow(() -> new FerosException("Fitting not found", HttpStatus.NOT_FOUND));

        if (!fitting.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Fitting not found", HttpStatus.NOT_FOUND);
        }
        if (fitting.getRemovedDate() != null) {
            throw new FerosException("Tyre already removed from this fitting", HttpStatus.CONFLICT);
        }

        User removedBy = getUser(userId);

        fitting.setRemovedAtKm(request.getRemovedAtKm());
        fitting.setRemovedDate(request.getRemovedDate() != null ? request.getRemovedDate() : TimeUtil.today());
        fitting.setRemovalReason(request.getRemovalReason());
        fitting.setRemovedBy(removedBy);
        fitting.setNotes(request.getNotes() != null ? request.getNotes() : fitting.getNotes());

        fittingRepository.save(fitting);

        // Accumulate km on tyre
        Tyre tyre = fitting.getTyre();
        if (request.getRemovedAtKm() != null && fitting.getFittedAtKm() != null) {
            BigDecimal km = request.getRemovedAtKm().subtract(fitting.getFittedAtKm());
            if (km.compareTo(BigDecimal.ZERO) > 0) {
                tyre.setTotalLifetimeKm(tyre.getTotalLifetimeKm().add(km));
            }
        }

        // Update tyre status based on removal reason
        TyreRemovalReason reason = request.getRemovalReason();
        if (reason == TyreRemovalReason.RETREAD) {
            tyre.setStatus(TyreStatus.RETREADING);
            tyre.setRetreadCount(tyre.getRetreadCount() + 1);
            tyre.setRetreaderName(request.getRetreaderName());
            tyre.setExpectedReturnDate(request.getExpectedReturnDate());
        } else if (reason == TyreRemovalReason.SCRAP) {
            tyre.setStatus(TyreStatus.SCRAPPED);
        } else {
            tyre.setStatus(TyreStatus.IN_STOCK);
        }
        tyreRepository.save(tyre);

        return toFittingResponse(fitting);
    }

    @Override
    public List<TyreFittingResponse> getFittingHistory(Long vehicleId) {
        return fittingRepository.findByVehicleIdAndIsActiveTrueOrderByFittedDateDescIdDesc(vehicleId)
                .stream().map(this::toFittingResponse).toList();
    }

    @Override
    public List<TyreFittingResponse> getTyreHistory(Long tyreId) {
        return fittingRepository.findByTyreIdAndIsActiveTrueOrderByFittedDateDescIdDesc(tyreId)
                .stream().map(this::toFittingResponse).toList();
    }

    // ── Rotations ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TyreRotationLogResponse performRotation(TyreRotationRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = getTenant(tenantId);
        Vehicle vehicle = getVehicle(request.getVehicleId(), tenantId);
        User performedBy = getUser(userId);

        List<TyreRotationRequest.TyreRotationMoveRequest> moves = request.getMoves();
        if (moves == null || moves.isEmpty()) {
            throw new FerosException("No moves specified for rotation", HttpStatus.BAD_REQUEST);
        }

        // Conflict check: no two tyres targeting the same destination position
        Set<Long> targetPositions = new HashSet<>();
        for (TyreRotationRequest.TyreRotationMoveRequest move : moves) {
            if (!targetPositions.add(move.getToPositionId())) {
                throw new FerosException("Two tyres cannot target the same destination position", HttpStatus.CONFLICT);
            }
        }

        // Create rotation log header
        TyreRotationLog rotationLog = TyreRotationLog.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .rotationDate(request.getRotationDate() != null ? request.getRotationDate() : TimeUtil.today())
                .odometerKm(request.getOdometerKm())
                .performedBy(performedBy)
                .notes(request.getNotes())
                .isActive(true)
                .build();
        rotationLog = rotationLogRepository.save(rotationLog);

        List<TyreRotationItem> items = new ArrayList<>();

        for (TyreRotationRequest.TyreRotationMoveRequest move : moves) {
            Tyre tyre = findTyre(move.getTyreId(), tenantId);
            VehicleTyrePosition fromPos = findPosition(move.getFromPositionId(), tenantId);
            VehicleTyrePosition toPos = findPosition(move.getToPositionId(), tenantId);

            // Find and close the old fitting
            VehicleTyreFitting oldFitting = fittingRepository
                    .findByPositionIdAndRemovedDateIsNullAndIsActiveTrue(fromPos.getId())
                    .orElseThrow(() -> new FerosException("No active fitting found for position: " + fromPos.getPositionCode(), HttpStatus.NOT_FOUND));

            oldFitting.setRemovedAtKm(request.getOdometerKm());
            oldFitting.setRemovedDate(request.getRotationDate() != null ? request.getRotationDate() : TimeUtil.today());
            oldFitting.setRemovalReason(TyreRemovalReason.ROTATION);
            oldFitting.setRemovedBy(performedBy);
            oldFitting.setRotationLog(rotationLog);
            fittingRepository.save(oldFitting);

            // Accumulate km on tyre
            if (oldFitting.getFittedAtKm() != null) {
                BigDecimal km = request.getOdometerKm().subtract(oldFitting.getFittedAtKm());
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    tyre.setTotalLifetimeKm(tyre.getTotalLifetimeKm().add(km));
                }
            }

            // Open new fitting at the target position
            VehicleTyreFitting newFitting = VehicleTyreFitting.builder()
                    .tenant(tenant)
                    .vehicle(vehicle)
                    .tyre(tyre)
                    .position(toPos)
                    .fittedAtKm(request.getOdometerKm())
                    .fittedDate(request.getRotationDate() != null ? request.getRotationDate() : TimeUtil.today())
                    .fittedBy(performedBy)
                    .rotationLog(rotationLog)
                    .isActive(true)
                    .build();
            newFitting = fittingRepository.save(newFitting);
            tyreRepository.save(tyre);

            TyreRotationItem item = TyreRotationItem.builder()
                    .rotationLog(rotationLog)
                    .tyre(tyre)
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
    public List<TyreRotationLogResponse> getRotationHistory(Long vehicleId) {
        return rotationLogRepository.findByVehicleIdAndIsActiveTrueOrderByRotationDateDescIdDesc(vehicleId)
                .stream().map(log -> {
                    List<TyreRotationItem> items = rotationItemRepository.findByRotationLogIdOrderById(log.getId());
                    return toRotationLogResponse(log, items);
                }).toList();
    }

    @Override
    @Transactional
    public TyreResponse markBackToStock(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tyre tyre = findTyre(id, tenantId);
        if (tyre.getStatus() != TyreStatus.RETREADING) {
            throw new FerosException("Only tyres in RETREADING status can be marked back to stock", HttpStatus.CONFLICT);
        }
        tyre.setStatus(TyreStatus.IN_STOCK);
        tyre.setRetreaderName(null);
        tyre.setExpectedReturnDate(null);
        return toTyreResponse(tyreRepository.save(tyre), null);
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

    private Tyre findTyre(Long tyreId, Long tenantId) {
        Tyre tyre = tyreRepository.findById(tyreId)
                .orElseThrow(() -> new FerosException("Tyre not found", HttpStatus.NOT_FOUND));
        if (!tyre.getTenant().getId().equals(tenantId) || !tyre.getIsActive()) {
            throw new FerosException("Tyre not found", HttpStatus.NOT_FOUND);
        }
        return tyre;
    }

    private VehicleTyrePosition findPosition(Long positionId, Long tenantId) {
        VehicleTyrePosition position = positionRepository.findById(positionId)
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

    private TyreResponse toTyreResponse(Tyre tyre, VehicleTyreFitting currentFitting) {
        return TyreResponse.builder()
                .id(tyre.getId())
                .tenantId(tyre.getTenant().getId())
                .serialNumber(tyre.getSerialNumber())
                .brand(tyre.getBrand())
                .size(tyre.getSize())
                .tyreType(tyre.getTyreType())
                .plyRating(tyre.getPlyRating())
                .purchaseDate(tyre.getPurchaseDate())
                .purchaseCost(tyre.getPurchaseCost())
                .status(tyre.getStatus())
                .retreadCount(tyre.getRetreadCount())
                .totalLifetimeKm(tyre.getTotalLifetimeKm())
                .notes(tyre.getNotes())
                .tyreLifeYears(tyre.getTyreLifeYears())
                .expiryDate(tyre.getExpiryDate())
                .maxLifetimeKm(tyre.getMaxLifetimeKm())
                .retreaderName(tyre.getRetreaderName())
                .expectedReturnDate(tyre.getExpectedReturnDate())
                .currentFittingId(currentFitting != null ? currentFitting.getId() : null)
                .currentVehicleRegistrationNumber(currentFitting != null ? currentFitting.getVehicle().getRegistrationNumber() : null)
                .currentPositionCode(currentFitting != null ? currentFitting.getPosition().getPositionCode() : null)
                .createdAt(tyre.getCreatedAt())
                .updatedAt(tyre.getUpdatedAt())
                .build();
    }

    private TyrePositionResponse toPositionResponse(VehicleTyrePosition position, VehicleTyreFitting currentFitting) {
        return TyrePositionResponse.builder()
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

    private TyreFittingResponse toFittingResponse(VehicleTyreFitting f) {
        return TyreFittingResponse.builder()
                .id(f.getId())
                .tenantId(f.getTenant().getId())
                .vehicleId(f.getVehicle().getId())
                .vehicleRegistrationNumber(f.getVehicle().getRegistrationNumber())
                .tyreId(f.getTyre().getId())
                .tyreSerialNumber(f.getTyre().getSerialNumber())
                .tyreBrand(f.getTyre().getBrand())
                .tyreSize(f.getTyre().getSize())
                .tyreMaxLifetimeKm(f.getTyre().getMaxLifetimeKm())
                .tyreTotalLifetimeKm(f.getTyre().getTotalLifetimeKm())
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

    private TyreRotationLogResponse toRotationLogResponse(TyreRotationLog log, List<TyreRotationItem> items) {
        return TyreRotationLogResponse.builder()
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

    private TyreRotationItemResponse toRotationItemResponse(TyreRotationItem item) {
        return TyreRotationItemResponse.builder()
                .id(item.getId())
                .tyreId(item.getTyre().getId())
                .tyreSerialNumber(item.getTyre().getSerialNumber())
                .fromPositionId(item.getFromPosition().getId())
                .fromPositionCode(item.getFromPosition().getPositionCode())
                .toPositionId(item.getToPosition().getId())
                .toPositionCode(item.getToPosition().getPositionCode())
                .oldFittingId(item.getOldFitting().getId())
                .newFittingId(item.getNewFitting().getId())
                .build();
    }
}
