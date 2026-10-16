package com.feros.api.enums;

/** Reason a client cut money from an order's bill (set only on DEDUCTION entries). */
public enum DeductionReason {
    FUEL,       // client paid diesel on our behalf and deducted it
    MAMULU,     // on-road payments the client covered
    PENALTY,    // late delivery / SLA cut
    SHORTAGE,   // short/damaged material claim
    MISC,
    OTHER
}
