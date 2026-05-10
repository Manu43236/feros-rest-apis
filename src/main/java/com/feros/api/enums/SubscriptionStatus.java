package com.feros.api.enums;

public enum SubscriptionStatus {
    TRIAL,
    ACTIVE,
    EXPIRED,
    SUSPENDED,
    RENEWED   // closed history row — superseded by a newer ACTIVE row
}