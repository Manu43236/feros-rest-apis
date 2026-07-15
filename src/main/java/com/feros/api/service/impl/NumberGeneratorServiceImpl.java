package com.feros.api.service.impl;

import com.feros.api.entity.DocumentSequence;
import com.feros.api.repository.DocumentSequenceRepository;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NumberGeneratorServiceImpl implements NumberGeneratorService {

    private final DocumentSequenceRepository sequenceRepo;

    @Override
    @Transactional
    public String generateFY(Long tenantId, NumberUtil.Type type) {
        String period = fyPeriod();
        long seq = nextSeq(tenantId, type.name(), period);
        return type.name() + tenantId + period + fmt(seq);
    }

    @Override
    @Transactional
    public String generateSequential(Long tenantId, NumberUtil.Type type) {
        long seq = nextSeq(tenantId, type.name(), "MASTER");
        return type.name() + tenantId + fmt(seq);
    }

    @Override
    @Transactional
    public String generateMonthly(Long tenantId, NumberUtil.Type type) {
        String period = monthlyPeriod();
        long seq = nextSeq(tenantId, type.name(), period);
        return type.name() + tenantId + period + fmt(seq);
    }

    private long nextSeq(Long tenantId, String docType, String period) {
        DocumentSequence row = sequenceRepo.findForUpdate(tenantId, docType, period)
                .orElseGet(() -> DocumentSequence.builder()
                        .tenantId(tenantId).docType(docType).period(period).lastSeq(0L).build());
        row.setLastSeq(row.getLastSeq() + 1);
        return sequenceRepo.save(row).getLastSeq();
    }

    // minimum 2-digit format: 01, 02 … 09, 10, 11 … 100 …
    private static String fmt(long n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    private static String fyPeriod() {
        LocalDateTime now = TimeUtil.nowIst();
        int year = now.getYear();
        if (now.getMonthValue() < 4) year--; // Jan–Mar belongs to previous FY
        return String.format("%02d%02d", year % 100, (year + 1) % 100);
    }

    private static String monthlyPeriod() {
        LocalDateTime now = TimeUtil.nowIst();
        return String.format("%02d%02d", now.getMonthValue(), now.getYear() % 100);
    }
}
