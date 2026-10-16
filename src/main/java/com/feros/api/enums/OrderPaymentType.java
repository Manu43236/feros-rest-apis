package com.feros.api.enums;

/** Type of money entry recorded against an order's settlement ledger. */
public enum OrderPaymentType {
    ADVANCE,    // money received upfront, before/while the order runs
    PAYMENT,    // money received post-delivery (balance / settlement)
    DEDUCTION   // client cut (fuel/mamulu/penalty/misc) — closes the balance, not cash received
}
