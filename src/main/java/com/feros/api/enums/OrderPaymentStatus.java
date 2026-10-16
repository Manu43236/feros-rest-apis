package com.feros.api.enums;

public enum OrderPaymentStatus {
    UNPAID,
    ADVANCE_PAID,
    PARTIALLY_PAID,
    PAID,
    SETTLED   // balance closed to zero via deductions (client cut the remainder)
}
