package com.corporate.payments.service;

import com.corporate.payments.exception.PurchaseTxNotFoundException;
import com.corporate.payments.repository.PurchaseTxRepository;
import com.corporate.payments.controller.PurchaseTxExchangeDTO;
import com.corporate.payments.valueObject.PurchaseTxVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class PurchaseTxService {

    @Autowired
    private PurchaseTxRepository purchaseTxRepository;
    @Autowired
    private RateOfExchangeService rateOfExchangeService;

    public PurchaseTxVO savePurchaseTx(@Valid PurchaseTxVO purchaseTxVO) {
        return purchaseTxRepository.save(purchaseTxVO);
    }

    public PurchaseTxVO getPurchaseTxById(Long id) {
        return purchaseTxRepository.findById(id)
                .orElseThrow(() -> new PurchaseTxNotFoundException(id));
    }

    public PurchaseTxExchangeDTO exchangePurchaseTx(Long id, String currency) {
        PurchaseTxVO purchaseTxVO = getPurchaseTxById(id);
        LocalDate transactionDate = purchaseTxVO.getTransactionDate();
        BigDecimal exchangeRate = rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, transactionDate.minusMonths(6), transactionDate);
        BigDecimal exchangedAmt = purchaseTxVO.getPurchaseAmt().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        return new PurchaseTxExchangeDTO(purchaseTxVO, currency, exchangeRate, exchangedAmt);
    }
}
