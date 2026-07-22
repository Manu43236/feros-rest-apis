package com.feros.api.util;

import java.math.BigDecimal;

public final class ProfessionalTaxUtil {

    private ProfessionalTaxUtil() {}

    /**
     * Returns the monthly Professional Tax amount for the given state and gross pay.
     * Returns ZERO for states that don't levy PT.
     */
    public static BigDecimal compute(String state, BigDecimal grossPay) {
        if (state == null || grossPay == null) return BigDecimal.ZERO;
        return switch (state.trim().toUpperCase()) {
            case "ANDHRA PRADESH", "TELANGANA" -> apTelangana(grossPay);
            case "KARNATAKA"                   -> karnataka(grossPay);
            case "MAHARASHTRA"                 -> maharashtra(grossPay);
            case "WEST BENGAL"                 -> westBengal(grossPay);
            case "GUJARAT"                     -> gujarat(grossPay);
            case "TAMIL NADU"                  -> tamilNadu(grossPay);
            case "MADHYA PRADESH"              -> madhyaPradesh(grossPay);
            case "ASSAM"                       -> assam(grossPay);
            // No PT: Delhi, UP, Rajasthan, Haryana, Bihar, Punjab, Himachal Pradesh, etc.
            default -> BigDecimal.ZERO;
        };
    }

    // AP / Telangana: ≤15,000 → 0 | 15,001–20,000 → 150 | >20,000 → 200
    private static BigDecimal apTelangana(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(15_000)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(20_000)) <= 0) return BigDecimal.valueOf(150);
        return BigDecimal.valueOf(200);
    }

    // Karnataka: ≤15,000 → 0 | 15,001–40,000 → 150 | >40,000 → 200
    private static BigDecimal karnataka(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(15_000)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(40_000)) <= 0) return BigDecimal.valueOf(150);
        return BigDecimal.valueOf(200);
    }

    // Maharashtra: ≤7,500 → 0 | 7,501–10,000 → 175 | >10,000 → 200 (300 in Feb)
    private static BigDecimal maharashtra(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(7_500)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(10_000)) <= 0) return BigDecimal.valueOf(175);
        return BigDecimal.valueOf(200);
    }

    // West Bengal: ≤10,000 → 0 | 10,001–15,000 → 110 | 15,001–25,000 → 130 | 25,001–40,000 → 150 | >40,000 → 200
    private static BigDecimal westBengal(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(10_000)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(15_000)) <= 0) return BigDecimal.valueOf(110);
        if (gross.compareTo(BigDecimal.valueOf(25_000)) <= 0) return BigDecimal.valueOf(130);
        if (gross.compareTo(BigDecimal.valueOf(40_000)) <= 0) return BigDecimal.valueOf(150);
        return BigDecimal.valueOf(200);
    }

    // Gujarat: ≤5,999 → 0 | 6,000–8,999 → 80 | 9,000–11,999 → 150 | ≥12,000 → 200
    private static BigDecimal gujarat(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(5_999)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(8_999)) <= 0) return BigDecimal.valueOf(80);
        if (gross.compareTo(BigDecimal.valueOf(11_999)) <= 0) return BigDecimal.valueOf(150);
        return BigDecimal.valueOf(200);
    }

    // Tamil Nadu: ≤21,000 → 0 | >21,000 → 208
    private static BigDecimal tamilNadu(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(21_000)) <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(208);
    }

    // Madhya Pradesh: ≤18,750 → 0 | >18,750 → 208
    private static BigDecimal madhyaPradesh(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(18_750)) <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(208);
    }

    // Assam: ≤10,000 → 0 | 10,001–15,000 → 150 | >15,000 → 208
    private static BigDecimal assam(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(10_000)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(15_000)) <= 0) return BigDecimal.valueOf(150);
        return BigDecimal.valueOf(208);
    }
}
