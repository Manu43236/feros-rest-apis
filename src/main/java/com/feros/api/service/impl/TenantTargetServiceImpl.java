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
import java.util.Objects;

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

    private TenantTargetResponse buildResponse(TenantTarget target, Long tenantId) {
        // Calculate actuals from LRs for the target period
        Integer year = target.getYear();
        Integer month = target.getMonth();

        int actualTrips = 0;
        BigDecimal actualTons = BigDecimal.ZERO;

        if (year != null && month != null) {
            LocalDate from = LocalDate.of(year, month, 1);
            LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
            List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);
            actualTrips = lrs.size();
            actualTons = lrs.stream()
                    .map(Lr::getLoadedWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Double tripsProgressPct = null;
        if (target.getTargetTrips() != null && target.getTargetTrips() > 0) {
            tripsProgressPct = BigDecimal.valueOf(actualTrips)
                    .divide(BigDecimal.valueOf(target.getTargetTrips()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        Double tonsProgressPct = null;
        if (target.getTargetTons() != null && target.getTargetTons().compareTo(BigDecimal.ZERO) > 0) {
            tonsProgressPct = actualTons
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
                .actualTrips(actualTrips)
                .actualTons(actualTons)
                .tripsProgressPct(tripsProgressPct)
                .tonsProgressPct(tonsProgressPct)
                .build();
    }
}
