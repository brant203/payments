package com.corporate.payments.exception;

public class ExternalServiceBadResponseException extends RuntimeException {
    public ExternalServiceBadResponseException(String message) {
        super(message);
    }
}
