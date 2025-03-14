package com.corporate.payments.exception;

public class PurchaseTxNotFoundException extends RuntimeException {
    public PurchaseTxNotFoundException(Long id) {
        super("Could not find purchase transaction with id: " + id);
    }
}
