package com.feros.api.util;

import com.feros.api.util.TimeUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating standardised document numbers across FEROS.
 *
 * Format: {tenantPrefix}_{typeCode}_{yyyyMMddHHmmss}
 * Example: ASHLAR_LR_20260217150759
 *
 * Vehicles are excluded from this scheme.
 */
public final class NumberUtil {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private NumberUtil() {}

    public enum Type {
        ORD,   // Order
        LR,    // Lorry Receipt
        INV,   // Invoice
        USR,   // User / Staff
        CLNT,  // Client
        DOC,   // Document
        ATD,   // Attendance
        PR,    // Payroll
        RPT,   // Report
        CN,    // Credit Note
        SVC,   // Vehicle Service
        PART,  // Spare Part
        WO,    // Work Order
        EINV,  // Equipment Invoice
        ESVC   // Equipment Service
    }

    /**
     * Generate a document number using the tenant's prefix.
     *
     * @param prefix  tenant prefix (e.g. "ASHLAR"); falls back to "T{tenantId}" if blank
     * @param tenantId used as fallback when prefix is null/blank
     * @param type    document type code
     * @return e.g. "ASHLAR_LR_20260217150759"
     */
    public static String generate(String prefix, Long tenantId, Type type) {
        String p = (prefix != null && !prefix.isBlank())
                ? prefix.trim().toUpperCase()
                : "T" + tenantId;
        return p + "_" + type.name() + "_" + TimeUtil.nowIst().format(TS);
    }
}
