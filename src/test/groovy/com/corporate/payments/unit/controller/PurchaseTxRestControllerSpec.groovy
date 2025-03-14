package com.corporate.payments.unit.controller

import com.corporate.payments.controller.PurchaseTxExchangeDTO
import com.corporate.payments.controller.PurchaseTxRestController
import com.corporate.payments.service.PurchaseTxService
import com.corporate.payments.valueObject.PurchaseTxVO
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.LocalDate

@ContextConfiguration(classes = [PurchaseTxRestController])
class PurchaseTxRestControllerSpec extends Specification {

    @SpringBean
    PurchaseTxService purchaseTxService = Mock()

    @Autowired
    PurchaseTxRestController purchaseTxRestController

    def "findById - happy path - requesting a purchase transaction by id returns the requested purchase transaction"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.id = 123
        purchaseTx.description = "description"
        purchaseTx.transactionDate = LocalDate.now()
        purchaseTx.purchaseAmt = 42.67

        when:
        PurchaseTxVO returnPurchaseTxVO =  purchaseTxRestController.findById(purchaseTx.id)

        then:
        1 * purchaseTxService.getPurchaseTxById(purchaseTx.id) >> purchaseTx
        0 * _

        and:
        returnPurchaseTxVO == purchaseTx
    }

    def "exchangePurchaseTx - happy path - requesting a purchase transaction exchange returns a PurchaeTxExchangeDTO containing the exchange rate and exchanged amount" () {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.id = 123
        purchaseTx.description = "description"
        purchaseTx.transactionDate = LocalDate.now()
        purchaseTx.purchaseAmt = 42.67

        String currency = "currency"
        BigDecimal exchangeRate = 1.5
        BigDecimal exchangedAmt = 64.00

        PurchaseTxExchangeDTO purchaseTxExchange = new PurchaseTxExchangeDTO()
        purchaseTxExchange.purchaseTxVO = purchaseTx
        purchaseTxExchange.currency = currency
        purchaseTxExchange.exchangeRate = exchangeRate
        purchaseTxExchange.exchangedAmt = exchangedAmt

        when:
        PurchaseTxExchangeDTO returnPurchaseTxExchangeDTO =  purchaseTxRestController.exchangePurchaseTx(purchaseTx.id, currency)

        then:
        1 * purchaseTxService.exchangePurchaseTx(purchaseTx.id, currency) >> purchaseTxExchange
        0 * _

        and:
        returnPurchaseTxExchangeDTO == purchaseTxExchange
    }

    def "newPurchaseTx - happy path - posting a new purchase transaction saves the purchase transaction and returns a created responseEntity containing the new transaction"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.id = 123
        purchaseTx.description = "description"
        purchaseTx.transactionDate = LocalDate.now()
        purchaseTx.purchaseAmt = 42.67

        when:
        ResponseEntity<?> response =  purchaseTxRestController.newPurchaseTx(purchaseTx)
        PurchaseTxVO returnPurchaseTxVO = response.body

        then:
        1 * purchaseTxService.savePurchaseTx(purchaseTx) >> purchaseTx
        0 * _

        and:
        response.statusCode == HttpStatus.CREATED
        returnPurchaseTxVO == purchaseTx
    }
}