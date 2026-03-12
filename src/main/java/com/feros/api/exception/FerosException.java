package com.feros.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FerosException extends RuntimeException {
    private final HttpStatus status;

    public FerosException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}