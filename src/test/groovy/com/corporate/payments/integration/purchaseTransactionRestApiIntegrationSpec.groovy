package com.corporate.payments.integration

import com.corporate.payments.PaymentsApplication
import com.corporate.payments.controller.PurchaseTxExchangeDTO
import com.corporate.payments.valueObject.PurchaseTxVO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.math.RoundingMode
import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest(classes = PaymentsApplication.class, properties = "spring.datasource.url=jdbc:mariadb://localhost:3306/payments")
class purchaseTransactionRestApiIntegrationSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())

    def "happy path - post new purchase transaction"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "happy path - post new purchase transaction"
        purchaseTx.transactionDate = LocalDate.of(2024, 7, 15)
        purchaseTx.purchaseAmt = 94872.99

        when: "post request is made"
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andReturn().response

        then:
        PurchaseTxVO returnPurchaseTx = objectMapper.readValue(response.contentAsString, PurchaseTxVO.class)

        response.status == HttpStatus.CREATED.value()
        returnPurchaseTx.id
        returnPurchaseTx.description == purchaseTx.description
        returnPurchaseTx.transactionDate == purchaseTx.transactionDate
        returnPurchaseTx.purchaseAmt == purchaseTx.purchaseAmt
    }

    def "happy path - get existing purchase transaction"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "happy path - get existing purchase transaction"
        purchaseTx.transactionDate = LocalDate.of(2024, 7, 15)
        purchaseTx.purchaseAmt = 94872.99

        MockHttpServletResponse postResponse = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn().response
        PurchaseTxVO postPurchaseTx = objectMapper.readValue(postResponse.contentAsString, PurchaseTxVO.class)

        when:
        MockHttpServletResponse getResponse = mockMvc.perform(
                get("/api/purchaseTx/{id}", postPurchaseTx.id))
                .andReturn().response
        PurchaseTxVO getResponsePurchaseTx = objectMapper.readValue(postResponse.contentAsString, PurchaseTxVO.class)

        then:
        getResponse.status == HttpStatus.OK.value()
        getResponsePurchaseTx.id
        getResponsePurchaseTx.description == purchaseTx.description
        getResponsePurchaseTx.transactionDate == purchaseTx.transactionDate
        getResponsePurchaseTx.purchaseAmt == purchaseTx.purchaseAmt
    }

    def "happy path - get exchanged transaction"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "happy path - get exchanged transaction"
        purchaseTx.transactionDate = LocalDate.of(2024, 7, 15)
        purchaseTx.purchaseAmt = 94872.99

        String currency = "Canada-Dollar"

        MockHttpServletResponse postResponse = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn().response
        PurchaseTxVO postPurchaseTx = objectMapper.readValue(postResponse.contentAsString, PurchaseTxVO.class)

        when:
        MockHttpServletResponse getExchangeResponse = mockMvc.perform(
                get("/api/purchaseTx/{id}/{currency}", postPurchaseTx.id, currency))
                .andReturn().response
        PurchaseTxExchangeDTO getResponsePurchaseTxExchange = objectMapper.readValue(getExchangeResponse.contentAsString, PurchaseTxExchangeDTO.class)

        then:
        getExchangeResponse.status == HttpStatus.OK.value()
        getResponsePurchaseTxExchange.purchaseTxVO.id
        getResponsePurchaseTxExchange.purchaseTxVO.description == purchaseTx.description
        getResponsePurchaseTxExchange.purchaseTxVO.transactionDate == purchaseTx.transactionDate
        getResponsePurchaseTxExchange.purchaseTxVO.purchaseAmt == purchaseTx.purchaseAmt
        getResponsePurchaseTxExchange.currency == currency
        getResponsePurchaseTxExchange.exchangeRate
        getResponsePurchaseTxExchange.exchangedAmt == (getResponsePurchaseTxExchange.exchangeRate * purchaseTx.purchaseAmt).setScale(2, RoundingMode.HALF_UP)
    }

    def "invalid post - description to long"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "This Description is too long......................."
        purchaseTx.transactionDate = LocalDate.of(2024, 7, 15)
        purchaseTx.purchaseAmt = 94872.99

        when:
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        response.contentAsString == "{\"errors\":[\"description must not exceed 50 characters\"]}"
    }

    def "invalid post - no transactionDate"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "invalid post - no transactionDate"
        purchaseTx.purchaseAmt = 94872.99

        when:
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        response.contentAsString == "{\"errors\":[\"transactionDate is required\"]}"
    }

    def "invalid post - future transactionDate"() {
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "invalid post - future transactionDate"
        purchaseTx.transactionDate = LocalDate.of(9999, 7, 15)
        purchaseTx.purchaseAmt = 94872.99

        when:
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        response.contentAsString == "{\"errors\":[\"transactionDate must be in the past\"]}"
    }

    def "invalid post - negative paymentTx"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "invalid post - negative paymentTx"
        purchaseTx.transactionDate = LocalDate.of(2024, 7, 15)
        purchaseTx.purchaseAmt = -94872.99

        when:
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        response.contentAsString == "{\"errors\":[\"purchaseAmt must be non-negative\"]}"
    }

    def "invalid get - transaction not found"() {
        given:
        Long id = -99999

        when:
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/purchaseTx/{id}", id))
                .andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        response.contentAsString == "Could not find purchase transaction with id: -99999"
    }

    def "invalid exchange - can not convert currency"() {
        given:
        PurchaseTxVO purchaseTx = new PurchaseTxVO()
        purchaseTx.description = "happy path - get exchanged transaction"
        purchaseTx.transactionDate = LocalDate.of(2024, 7, 15)
        purchaseTx.purchaseAmt = 94872.99

        String currency = "fake-currency"

        MockHttpServletResponse postResponse = mockMvc.perform(
                post("/api/purchaseTx")
                        .content(objectMapper.writeValueAsBytes(purchaseTx))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn().response
        PurchaseTxVO postPurchaseTx = objectMapper.readValue(postResponse.contentAsString, PurchaseTxVO.class)

        when:
        MockHttpServletResponse getExchangeResponse = mockMvc.perform(
                get("/api/purchaseTx/{id}/{currency}", postPurchaseTx.id, currency))
                .andReturn().response

        then:
        getExchangeResponse.status == HttpStatus.BAD_REQUEST.value()
        getExchangeResponse.contentAsString == "The purchase cannot be converted to the target currency"
    }

    def "invalid exchange - transaction not found"() {
        given:
        Long id = -99999
        String currency = "Canada-Dollar"

        when:
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/purchaseTx/{id}/{currency}", id, currency))
                .andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        response.contentAsString == "Could not find purchase transaction with id: -99999"
    }
}