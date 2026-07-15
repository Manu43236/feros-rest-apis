package com.feros.api.service;

import com.feros.api.util.NumberUtil;

public interface NumberGeneratorService {
    String generateFY(Long tenantId, NumberUtil.Type type);
    String generateSequential(Long tenantId, NumberUtil.Type type);
    String generateMonthly(Long tenantId, NumberUtil.Type type);
}
