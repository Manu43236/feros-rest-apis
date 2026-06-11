package com.feros.api.exception;

public class WrongPinException extends RuntimeException {

    private final int failedAttempts;

    public WrongPinException(int failedAttempts) {
        super("Invalid mobile number or PIN");
        this.failedAttempts = failedAttempts;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }
}
