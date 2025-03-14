package com.corporate.payments.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "payment")
public class PurchaseTxVO {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    @Size(max = 50, message = "description must not exceed 50 characters")
    private String description;

    @PastOrPresent(message = "transactionDate must be in the past")
    @NotNull(message = "transactionDate is required")
    private LocalDate transactionDate;

    @PositiveOrZero(message = "purchaseAmt must be non-negative")
    @NotNull(message = "purchaseAmt is required")
    private BigDecimal purchaseAmt;

    PurchaseTxVO() {}

    PurchaseTxVO(String description, LocalDate transactionDate, BigDecimal purchaseAmt) {
        this.description = description;
        this.transactionDate = transactionDate;
        this.purchaseAmt = purchaseAmt.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getPurchaseAmt() {
        return purchaseAmt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setPurchaseAmt(BigDecimal purchaseAmt) {
        this.purchaseAmt = purchaseAmt.setScale(2, RoundingMode.HALF_UP);
    }

}
