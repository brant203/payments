package com.corporate.payments.service;

import com.corporate.payments.exception.ExchangeRateNotFoundException;
import com.corporate.payments.exception.ExternalServiceBadResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;

@Service
public class RateOfExchangeService {

    private final static String ratesOfExchangeQueryUri =
            "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange" +
                    "?fields=country_currency_desc,exchange_rate,record_date" +
                    "&filter=country_currency_desc:eq:%s,record_date:gte:%tF,record_date:lte:%tF&sort=-record_date";
    private final static String NODE_FIELD = "/data";

    @Autowired
    private RestTemplate restTemplate;


    public BigDecimal getExchangeRateForCurrencyBetweenDates(String currency, LocalDate startDate, LocalDate endDate) {

        String rateOfExchangeResponseBody = getRateOfExchangeResponseBody(currency, startDate, endDate);

        fiscalDataExchangeRate[] exchangeRates;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(rateOfExchangeResponseBody);
            JsonNode dataNode = node.at(NODE_FIELD);
            exchangeRates = mapper.treeToValue(dataNode, fiscalDataExchangeRate[].class);
        } catch (JsonProcessingException e) {
            throw new ExternalServiceBadResponseException("Unexpected response from treasury rates of exchange api - unable to parse body");
        }

        if(exchangeRates.length > 0 && exchangeRates[0].exchange_rate != null)
            return exchangeRates[0].exchange_rate;
        else {
            throw new ExchangeRateNotFoundException();
        }
    }

    private String getRateOfExchangeResponseBody(String currency, LocalDate startDate, LocalDate endDate) {
        String formattedUri = String.format(ratesOfExchangeQueryUri, currency, startDate, endDate);
        ResponseEntity<?> rateOfExchangeResponseEntity = restTemplate.getForEntity(formattedUri, String.class);

        if(rateOfExchangeResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new ExternalServiceBadResponseException("Unexpected response from treasury rates of exchange api - response status not 200");
        }
        if(!rateOfExchangeResponseEntity.hasBody()) {
            throw new ExternalServiceBadResponseException("Unexpected response from treasury rates of exchange api - body empty");
        }

        return rateOfExchangeResponseEntity.getBody().toString();
    }

    private static class fiscalDataExchangeRate {
        private String country_currency_desc;
        private BigDecimal exchange_rate;
        private Calendar record_date;

        String getCountry_currency_desc() {
            return country_currency_desc;
        }

        BigDecimal getExchange_rate() {
            return exchange_rate;
        }

        Calendar getRecord_date() {
            return record_date;
        }

        void setCountry_currency_desc(String country_currency_desc) {
            this.country_currency_desc = country_currency_desc;
        }

        void setExchange_rate(BigDecimal exchange_rate) {
            this.exchange_rate = exchange_rate;
        }

        void setRecord_date(Calendar record_date) {
            this.record_date = record_date;
        }

    }

}
