package com.corporate.payments.controller;

import com.corporate.payments.valueObject.PurchaseTxVO;

import java.math.BigDecimal;

public class PurchaseTxExchangeDTO {
    private PurchaseTxVO purchaseTxVO;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal exchangedAmt;

    PurchaseTxExchangeDTO(){}

    public PurchaseTxExchangeDTO(PurchaseTxVO purchaseTxVO, String currency, BigDecimal exchangeRate, BigDecimal exchangedAmt) {
        this.purchaseTxVO = purchaseTxVO;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.exchangedAmt = exchangedAmt;
    }

    public PurchaseTxVO getPurchaseTxVO() {
        return purchaseTxVO;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getExchangedAmt() {
        return exchangedAmt;
    }

    public void setPurchaseTxVO(PurchaseTxVO purchaseTxVO) {
        this.purchaseTxVO = purchaseTxVO;
    }

    public String getCurrency() {
        return currency;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setExchangedAmt(BigDecimal exchangedAmt) {
        this.exchangedAmt = exchangedAmt;
    }
}
