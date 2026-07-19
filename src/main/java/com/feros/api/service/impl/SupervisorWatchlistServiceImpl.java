package com.feros.api.service.impl;

import com.feros.api.dto.response.UserResponse;
import com.feros.api.dto.response.VehicleResponse;
import com.feros.api.entity.SupervisorStaffWatchlist;
import com.feros.api.entity.SupervisorVehicleWatchlist;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.User;
import com.feros.api.entity.Vehicle;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.SupervisorStaffWatchlistRepository;
import com.feros.api.repository.SupervisorVehicleWatchlistRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.UserRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.service.SupervisorWatchlistService;
import com.feros.api.service.UserService;
import com.feros.api.service.VehicleService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupervisorWatchlistServiceImpl implements SupervisorWatchlistService {

    private final SupervisorVehicleWatchlistRepository vehicleWatchlistRepo;
    private final SupervisorStaffWatchlistRepository   staffWatchlistRepo;
    private final TenantRepository                     tenantRepository;
    private final UserRepository                       userRepository;
    private final VehicleRepository                    vehicleRepository;
    private final VehicleService                       vehicleService;
    private final UserService                          userService;

    // ── Vehicle watchlist ──────────────────────────────────────────────────────

    @Override
    public List<VehicleResponse> getVehicleWatchlist() {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();
        return vehicleWatchlistRepo
                .findVehicleIdsByTenantIdAndSupervisorId(tenantId, supervisorId)
                .stream()
                .map(vehicleService::getVehicleById)
                .toList();
    }

    @Override
    @Transactional
    public VehicleResponse addVehicleToWatchlist(Long vehicleId) {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();

        if (vehicleWatchlistRepo.existsByTenantIdAndSupervisorIdAndVehicleId(tenantId, supervisorId, vehicleId)) {
            return vehicleService.getVehicleById(vehicleId);
        }

        Tenant  tenant     = resolveTenant(tenantId);
        User    supervisor = resolveUser(supervisorId);
        Vehicle vehicle    = vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        vehicleWatchlistRepo.save(SupervisorVehicleWatchlist.builder()
                .tenant(tenant)
                .supervisor(supervisor)
                .vehicle(vehicle)
                .build());

        return vehicleService.getVehicleById(vehicleId);
    }

    @Override
    @Transactional
    public void removeVehicleFromWatchlist(Long vehicleId) {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();
        vehicleWatchlistRepo.deleteByTenantIdAndSupervisorIdAndVehicleId(tenantId, supervisorId, vehicleId);
    }

    @Override
    public Set<Long> getWatchlistedVehicleIds() {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();
        return new HashSet<>(vehicleWatchlistRepo
                .findVehicleIdsByTenantIdAndSupervisorId(tenantId, supervisorId));
    }

    // ── Staff watchlist ────────────────────────────────────────────────────────

    @Override
    public List<UserResponse> getStaffWatchlist() {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();
        return staffWatchlistRepo
                .findUserIdsByTenantIdAndSupervisorId(tenantId, supervisorId)
                .stream()
                .map(userService::getUserById)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse addStaffToWatchlist(Long userId) {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();

        if (staffWatchlistRepo.existsByTenantIdAndSupervisorIdAndStaffUserId(tenantId, supervisorId, userId)) {
            return userService.getUserById(userId);
        }

        Tenant tenant     = resolveTenant(tenantId);
        User   supervisor = resolveUser(supervisorId);
        User   staffUser  = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        staffWatchlistRepo.save(SupervisorStaffWatchlist.builder()
                .tenant(tenant)
                .supervisor(supervisor)
                .staffUser(staffUser)
                .build());

        return userService.getUserById(userId);
    }

    @Override
    @Transactional
    public void removeStaffFromWatchlist(Long userId) {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();
        staffWatchlistRepo.deleteByTenantIdAndSupervisorIdAndStaffUserId(tenantId, supervisorId, userId);
    }

    @Override
    public Set<Long> getWatchlistedStaffIds() {
        Long tenantId     = SecurityUtil.getCurrentTenantId();
        Long supervisorId = SecurityUtil.getCurrentUserId();
        return new HashSet<>(staffWatchlistRepo
                .findUserIdsByTenantIdAndSupervisorId(tenantId, supervisorId));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Tenant resolveTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));
    }
}
