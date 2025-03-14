package com.corporate.payments.unit.service

import com.corporate.payments.controller.PurchaseTxExchangeDTO
import com.corporate.payments.exception.PurchaseTxNotFoundException
import com.corporate.payments.repository.PurchaseTxRepository
import com.corporate.payments.service.PurchaseTxService
import com.corporate.payments.service.RateOfExchangeService
import com.corporate.payments.valueObject.PurchaseTxVO
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.LocalDate

@ContextConfiguration(classes = [PurchaseTxService])
class PurchaseTxServiceSpec extends Specification {

    @SpringBean
    PurchaseTxRepository purchaseTxRepository = Mock()

    @SpringBean
    RateOfExchangeService rateOfExchangeService = Mock()

    @Autowired
    PurchaseTxService purchaseTxService

    def "savePurchaseTx - happy path - purchaseTxVO is saved to the DB and the saved purchaseTxVO is returned"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "description"
        purchaseTx.transactionDate = LocalDate.now()
        purchaseTx.purchaseAmt = BigDecimal.valueOf(69.69)

        when:
        PurchaseTxVO returnPurchaseTx = purchaseTxService.savePurchaseTx(purchaseTx)

        then:
        1 * purchaseTxRepository.save(purchaseTx) >> purchaseTx
        0 * _

        and:
        returnPurchaseTx == purchaseTx
    }

    def "getPurchaseTxById - happy path - requesting a purchaseTx by id returns the requested purchaseTx"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        Long id = 123
        purchaseTx.description = "description"
        purchaseTx.transactionDate = LocalDate.now()
        purchaseTx.purchaseAmt = 69.69

        when:
        PurchaseTxVO returnPurchaseTx = purchaseTxService.getPurchaseTxById(id)

        then:
        1 * purchaseTxRepository.findById(id) >> Optional.of(purchaseTx)
        0 * _

        and:
        returnPurchaseTx == purchaseTx
    }

    def "getPurchaseTxById - not found - requesting a purchaseTx by id that does not exist throws an exception"(){
        when:
        purchaseTxService.getPurchaseTxById(1)

        then:
        1 * purchaseTxRepository.findById(1) >> Optional.empty()
        0 * _

        and:
        thrown(PurchaseTxNotFoundException)
    }

    def "purchaseTxExchange - transaction date in the second half of the year - requesting a currency exchange for a transaction ID returns a PurchaseTxExchangeDTO with expected fields including a exchangedAmt that has been rounded up"() {
        given:
        LocalDate startDate = LocalDate.of(2024, 1, 1)
        LocalDate transactionDate = LocalDate.of(2024, 7, 1)

        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "description"
        purchaseTx.transactionDate = transactionDate
        purchaseTx.purchaseAmt = 69.69

        String currency = "Canada-Dollar"
        BigDecimal exchangeRate = BigDecimal.valueOf(1.5)

        when:
        PurchaseTxExchangeDTO purchaseTxExchange = purchaseTxService.exchangePurchaseTx(purchaseTx.id, currency)

        then:
        1 * purchaseTxRepository.findById(purchaseTx.id) >> Optional.of(purchaseTx)
        1 * rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, transactionDate) >> exchangeRate
        0 * _

        and:
        purchaseTxExchange.purchaseTxVO == purchaseTx
        purchaseTxExchange.currency == currency
        purchaseTxExchange.exchangeRate == exchangeRate
        purchaseTxExchange.exchangedAmt == BigDecimal.valueOf(104.54) //transactionAmt * exchange rate rounded to 2 decimal points, 69.69 * 1.5 = 104.535 rounded = 104.54
    }

    def "purchaseTxExchange - transaction date in the first half of the year - requesting a currency exchange for a transaction ID returns a PurchaseTxExchangeDTO with expected fields including a exchangedAmt that has been rounded down"() {
        given:
        LocalDate startDate = LocalDate.of(2024, 7, 1)
        LocalDate transactionDate = LocalDate.of(2025, 1, 1)

        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "description"
        purchaseTx.transactionDate = transactionDate
        purchaseTx.purchaseAmt = 49.01

        String currency = "Canada-Dollar"
        BigDecimal exchangeRate = BigDecimal.valueOf(1.4)

        when:
        PurchaseTxExchangeDTO purchaseTxExchange = purchaseTxService.exchangePurchaseTx(purchaseTx.id, currency)

        then:
        1 * purchaseTxRepository.findById(purchaseTx.id) >> Optional.of(purchaseTx)
        1 * rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, transactionDate) >> exchangeRate
        0 * _

        and:
        purchaseTxExchange.purchaseTxVO == purchaseTx
        purchaseTxExchange.currency == currency
        purchaseTxExchange.exchangeRate == exchangeRate
        purchaseTxExchange.exchangedAmt == BigDecimal.valueOf(68.61) //transactionAmt * exchange rate rounded to 2 decimal points, 69.69 * 1.5 = 104.535 rounded = 104.54
    }

    def "purchaseTxExchange - transaction date on march 31st - requesting a currency exchange for a transaction ID returns a PurchaseTxExchangeDTO with expected fields"() {
        given:
        LocalDate startDate = LocalDate.of(2023, 9, 30)
        LocalDate transactionDate = LocalDate.of(2024, 3, 31)

        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "description"
        purchaseTx.transactionDate = transactionDate
        purchaseTx.purchaseAmt = 69.69

        String currency = "Canada-Dollar"
        BigDecimal exchangeRate = BigDecimal.valueOf(1.5)

        when:
        PurchaseTxExchangeDTO purchaseTxExchange = purchaseTxService.exchangePurchaseTx(purchaseTx.id, currency)

        then:
        1 * purchaseTxRepository.findById(purchaseTx.id) >> Optional.of(purchaseTx)
        1 * rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, transactionDate) >> exchangeRate
        0 * _

        and:
        purchaseTxExchange.purchaseTxVO == purchaseTx
        purchaseTxExchange.currency == currency
        purchaseTxExchange.exchangeRate == exchangeRate
        purchaseTxExchange.exchangedAmt == BigDecimal.valueOf(104.54) //transactionAmt * exchange rate rounded to 2 decimal points, 69.69 * 1.5 = 104.535 rounded = 104.54
    }
}