package com.corporate.payments.unit.service

import com.corporate.payments.exception.ExchangeRateNotFoundException
import com.corporate.payments.exception.ExternalServiceBadResponseException
import com.corporate.payments.service.RateOfExchangeService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.LocalDate

@ContextConfiguration(classes = [RateOfExchangeService])
class RateOfExchangeServiceSpec extends Specification {

    @SpringBean
    RestTemplate restTemplate = Mock()

    @Autowired
    RateOfExchangeService rateOfExchangeService

    def "getExchangeRateForCurrencyBetweenDates - happy path - requesting a currency exchange rate returns the most recent rate from the treasury.gov api within the given dates"() {
        given:
        LocalDate startDate = LocalDate.of(2024, 1, 1)
        LocalDate endDate = LocalDate.of(2024, 6, 1)
        String currency = "Canada-Dollar"

        String responseBody = "{\"data\":[{\"country_currency_desc\":\"Canada-Dollar\",\"exchange_rate\":\"1.532\",\"record_date\":\"2001-03-31\"},{\"country_currency_desc\":\"1.532\",\"exchange_rate\":\"1.541\",\"record_date\":\"2001-06-30\"}],\"meta\":{\"count\":2,\"labels\":{\"country_currency_desc\":\"Country-CurrencyDescription\",\"exchange_rate\":\"ExchangeRate\",\"record_date\":\"RecordDate\"},\"dataTypes\":{\"country_currency_desc\":\"STRING\",\"exchange_rate\":\"NUMBER\",\"record_date\":\"DATE\"},\"dataFormats\":{\"country_currency_desc\":\"String\",\"exchange_rate\":\"10.2\",\"record_date\":\"YYYY-MM-DD\"},\"total-count\":2,\"total-pages\":1},\"links\":{\"self\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"first\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"prev\":null,\"next\":null,\"last\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\"}}"
        ResponseEntity<?> response = ResponseEntity.ok(responseBody)

        String rateOfExchangeQueryURI = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange" +
                "?fields=country_currency_desc,exchange_rate,record_date" +
                "&filter=country_currency_desc:eq:Canada-Dollar,record_date:gte:2024-01-01,record_date:lte:2024-06-01&sort=-record_date"

        when:
        BigDecimal returnRateOfExchange = rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, endDate)

        then:
        1 * restTemplate.getForEntity(rateOfExchangeQueryURI, String.class) >> response
        0 * _

        and:
        returnRateOfExchange == 1.532
    }

    def "getExchangeRateForCurrencyBetweenDates - bad response status from treasury api - When the treasury.gov api does to return an OK status throw an ExternalServiceBadResponseException"() {
        given:
        LocalDate startDate = LocalDate.of(2024, 1, 1)
        LocalDate endDate = LocalDate.of(2024, 6, 1)
        String currency = "Canada-Dollar"

        String rateOfExchangeQueryURI = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange" +
                "?fields=country_currency_desc,exchange_rate,record_date" +
                "&filter=country_currency_desc:eq:Canada-Dollar,record_date:gte:2024-01-01,record_date:lte:2024-06-01&sort=-record_date"

        ResponseEntity<?> response = ResponseEntity.internalServerError().build()

        when:
        rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, endDate)

        then:
        1 * restTemplate.getForEntity(rateOfExchangeQueryURI, String.class) >> response
        0 * _

        and:
        thrown(ExternalServiceBadResponseException)
    }

    def "getExchangeRateForCurrencyBetweenDates - empty response body from treasury api - When the treasury.gov api response body is empty throw an ExternalServiceBadResponseException"() {
        given:
        LocalDate startDate = LocalDate.of(2024, 1, 1)
        LocalDate endDate = LocalDate.of(2024, 6, 1)
        String currency = "Canada-Dollar"

        String rateOfExchangeQueryURI = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange" +
                "?fields=country_currency_desc,exchange_rate,record_date" +
                "&filter=country_currency_desc:eq:Canada-Dollar,record_date:gte:2024-01-01,record_date:lte:2024-06-01&sort=-record_date"

        ResponseEntity<?> response = ResponseEntity.ok().build()

        when:
        rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, endDate)

        then:
        1 * restTemplate.getForEntity(rateOfExchangeQueryURI, String.class) >> response
        0 * _

        and:
        thrown(ExternalServiceBadResponseException)
    }

    def "getExchangeRateForCurrencyBetweenDates - no results from treasury api - When the treasury.gov api response does not contain any exchange rates throw an ExternalServiceBadResponseException"() {
        given:
        LocalDate startDate = LocalDate.of(2024, 1, 1)
        LocalDate endDate = LocalDate.of(2024, 6, 1)
        String currency = "Canada-Dollar"

        String rateOfExchangeQueryURI = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange" +
                "?fields=country_currency_desc,exchange_rate,record_date" +
                "&filter=country_currency_desc:eq:Canada-Dollar,record_date:gte:2024-01-01,record_date:lte:2024-06-01&sort=-record_date"

        String responseBody = "{\"data\":[],\"meta\":{\"count\":0,\"labels\":{\"country_currency_desc\":\"Country-CurrencyDescription\",\"exchange_rate\":\"ExchangeRate\",\"record_date\":\"RecordDate\"},\"dataTypes\":{\"country_currency_desc\":\"STRING\",\"exchange_rate\":\"NUMBER\",\"record_date\":\"DATE\"},\"dataFormats\":{\"country_currency_desc\":\"String\",\"exchange_rate\":\"10.2\",\"record_date\":\"YYYY-MM-DD\"},\"total-count\":0,\"total-pages\":0},\"links\":{\"self\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"first\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"prev\":null,\"next\":\"&page%5Bnumber%5D=2&page%5Bsize%5D=100\",\"last\":\"&page%5Bnumber%5D=0&page%5Bsize%5D=100\"}}"
        ResponseEntity<?> response = ResponseEntity.ok(responseBody)

        when:
        rateOfExchangeService.getExchangeRateForCurrencyBetweenDates(currency, startDate, endDate)

        then:
        1 * restTemplate.getForEntity(rateOfExchangeQueryURI, String.class) >> response
        0 * _

        and:
        thrown(ExchangeRateNotFoundException)
    }
}