package com.feros.api.service.impl;

import com.feros.api.dto.request.TenantTargetRequest;
import com.feros.api.dto.response.TenantTargetResponse;
import com.feros.api.entity.Lr;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.TenantTarget;
import com.feros.api.repository.LrRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.TenantTargetRepository;
import com.feros.api.service.TenantTargetService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantTargetServiceImpl implements TenantTargetService {

    private final TenantTargetRepository targetRepository;
    private final TenantRepository tenantRepository;
    private final LrRepository lrRepository;

    @Override
    @Transactional
    public TenantTargetResponse setTarget(TenantTargetRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        TenantTarget target = targetRepository
                .findByTenantIdAndYearAndMonth(tenantId, request.getYear(), request.getMonth())
                .orElse(TenantTarget.builder().tenant(tenant).year(request.getYear()).month(request.getMonth()).build());

        target.setTargetTrips(request.getTargetTrips());
        target.setTargetTons(request.getTargetTons());
        target = targetRepository.save(target);

        return buildResponse(target, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantTargetResponse getTarget(Integer year, Integer month) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        TenantTarget target = targetRepository
                .findByTenantIdAndYearAndMonth(tenantId, year, month)
                .orElse(TenantTarget.builder().year(year).month(month).build());
        return buildResponse(target, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantTargetResponse> getAllTargets() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return targetRepository.findByTenantIdOrderByYearDescMonthDesc(tenantId)
                .stream()
                .map(t -> buildResponse(t, tenantId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TenantTargetResponse getCurrentMonthTarget() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = LocalDate.now();
        int year  = today.getYear();
        int month = today.getMonthValue();

        // Try current month; if not set, carry forward from previous month
        TenantTarget target = targetRepository
                .findByTenantIdAndYearAndMonth(tenantId, year, month)
                .orElseGet(() -> {
                    LocalDate prev = today.minusMonths(1);
                    return targetRepository
                            .findByTenantIdAndYearAndMonth(tenantId, prev.getYear(), prev.getMonthValue())
                            .map(t -> TenantTarget.builder()
                                    .year(year).month(month)
                                    .targetTrips(t.getTargetTrips())
                                    .targetTons(t.getTargetTons())
                                    .build())
                            .orElse(TenantTarget.builder().year(year).month(month).build());
                });

        return buildResponse(target, tenantId);
    }

    private TenantTargetResponse buildResponse(TenantTarget target, Long tenantId) {
        Integer year  = target.getYear();
        Integer month = target.getMonth();

        int completedTrips = 0, localTrips = 0, nonLocalTrips = 0;
        BigDecimal completedTons = BigDecimal.ZERO;
        BigDecimal localTons     = BigDecimal.ZERO;
        BigDecimal nonLocalTons  = BigDecimal.ZERO;

        if (year != null && month != null) {
            LocalDate from = LocalDate.of(year, month, 1);
            LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

            List<Lr> deliveredLrs = lrRepository.findByTenantIdAndLrStatusAndDateRange(
                    tenantId, com.feros.api.enums.LrStatus.DELIVERED, from, to);

            for (Lr lr : deliveredLrs) {
                completedTrips++;
                BigDecimal weight = lr.getLoadedWeight() != null ? lr.getLoadedWeight()
                        : (lr.getAllocatedWeight() != null ? lr.getAllocatedWeight() : BigDecimal.ZERO);
                completedTons = completedTons.add(weight);

                boolean isLocal = lr.getOrder().getSourceState() != null
                        && lr.getOrder().getDestinationState() != null
                        && lr.getOrder().getSourceState().getId()
                               .equals(lr.getOrder().getDestinationState().getId());
                if (isLocal) {
                    localTrips++;
                    localTons = localTons.add(weight);
                } else {
                    nonLocalTrips++;
                    nonLocalTons = nonLocalTons.add(weight);
                }
            }
        }

        int pendingTrips = 0;
        BigDecimal pendingTons = BigDecimal.ZERO;
        if (target.getTargetTrips() != null) {
            pendingTrips = Math.max(0, target.getTargetTrips() - completedTrips);
        }
        if (target.getTargetTons() != null) {
            pendingTons = target.getTargetTons().subtract(completedTons).max(BigDecimal.ZERO);
        }

        Double tripsProgressPct = null;
        if (target.getTargetTrips() != null && target.getTargetTrips() > 0) {
            tripsProgressPct = BigDecimal.valueOf(completedTrips)
                    .divide(BigDecimal.valueOf(target.getTargetTrips()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        Double tonsProgressPct = null;
        if (target.getTargetTons() != null && target.getTargetTons().compareTo(BigDecimal.ZERO) > 0) {
            tonsProgressPct = completedTons
                    .divide(target.getTargetTons(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return TenantTargetResponse.builder()
                .id(target.getId())
                .year(year)
                .month(month)
                .targetTrips(target.getTargetTrips())
                .targetTons(target.getTargetTons())
                .completedTrips(completedTrips)
                .pendingTrips(pendingTrips)
                .localTrips(localTrips)
                .nonLocalTrips(nonLocalTrips)
                .completedTons(completedTons)
                .pendingTons(pendingTons)
                .localTons(localTons)
                .nonLocalTons(nonLocalTons)
                .tripsProgressPct(tripsProgressPct)
                .tonsProgressPct(tonsProgressPct)
                .build();
    }
}
