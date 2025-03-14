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

        URL validResponseUrl = getClass().getResource("treasuryValidResponse.json")
        String responseBody = new File(validResponseUrl.getPath()).text
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

        URL validResponseUrl = getClass().getResource("treasuryNoResultsResponse.json")
        String responseBody = new File(validResponseUrl.getPath()).text
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