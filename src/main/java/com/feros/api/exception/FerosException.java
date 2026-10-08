package com.feros.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FerosException extends RuntimeException {
    private final HttpStatus status;
    /** Optional machine-readable code so the client can branch (e.g. SWAPPABLE_CONFLICT vs HARD_BLOCK). */
    private final String code;

    public FerosException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public FerosException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
