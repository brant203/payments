package com.corporate.payments.exception;

public class ExchangeRateNotFoundException extends RuntimeException {
  public ExchangeRateNotFoundException() {
    super("The purchase cannot be converted to the target currency");
  }
}
