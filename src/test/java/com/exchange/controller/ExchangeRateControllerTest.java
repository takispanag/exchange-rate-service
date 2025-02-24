package com.exchange.controller;

import com.exchange.config.CurrencyConfig;
import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.exception.CurrencyConversionException;
import com.exchange.exception.ExchangeRateException;
import com.exchange.model.CurrencyExchangeRequest;
import com.exchange.service.ExchangeRateProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
@Import(CurrencyConfig.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateProviderService exchangeRateService;

    @MockitoBean
    private CurrencyConfig currencyConfig;

    private SingleExchangeRateDto singleRateResult;
    private AllExchangeRatesDto allRatesResult;

    @BeforeEach
    void setUp() {
        when(currencyConfig.getSupported())
                .thenReturn(Set.of("USD", "EUR", "GBP"));

        singleRateResult = SingleExchangeRateDto.builder()
                .sourceCurrency("USD")
                .targetCurrency("EUR")
                .exchangeRate(BigDecimal.valueOf(0.85))
                .timestamp(Instant.now())
                .build();

        allRatesResult = AllExchangeRatesDto.builder()
                .sourceCurrency("USD")
                .rates(Map.of(
                        "EUR", BigDecimal.valueOf(0.85),
                        "GBP", BigDecimal.valueOf(0.73)))
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void getExchangeRate_ValidRequest_ReturnsRate() throws Exception {
        when(exchangeRateService.getExchangeRate(any(), any()))
                .thenReturn(singleRateResult);

        mockMvc.perform(get("/api/v1/exchange/rates/single")
                        .param("sourceCurrency", "USD")
                        .param("targetCurrency", "EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.exchangeRate").value(0.85))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getExchangeRate_InvalidCurrency_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/rates/single")
                        .param("sourceCurrency", "")
                        .param("targetCurrency", "EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRates_ValidRequest_ReturnsAllRates() throws Exception {
        when(exchangeRateService.getAllRates(any()))
                .thenReturn(allRatesResult);

        mockMvc.perform(get("/api/v1/exchange/rates/all")
                        .param("currency", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.rates.EUR").value(0.85))
                .andExpect(jsonPath("$.rates.GBP").value(0.73))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getAllRates_InvalidCurrency_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/rates/all")
                        .param("currency", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getExchangeRate_WhenRateNotFound_ReturnsBadRequest() throws Exception {
        when(exchangeRateService.getExchangeRate(any(), any()))
                .thenThrow(new CurrencyConversionException("Rate not found"));

        mockMvc.perform(get("/api/v1/exchange/rates/single")
                        .param("sourceCurrency", "USD")
                        .param("targetCurrency", "EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Currency conversion failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getAllRates_WhenInvalidCurrency_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/rates/all")
                        .param("currency", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void whenUnexpectedError_ReturnsInternalServerError() throws Exception {
        when(exchangeRateService.getExchangeRate(any(), any()))
                .thenThrow(new RuntimeException("Unexpected internal error"));

        mockMvc.perform(get("/api/v1/exchange/rates/single")
                        .param("sourceCurrency", "USD")
                        .param("targetCurrency", "EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenResourceNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/rates/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getExchangeRate_WhenServiceThrowsException_ReturnsServiceUnavailable() throws Exception {
        CurrencyExchangeRequest request = new CurrencyExchangeRequest();
        request.setSourceCurrency("USD");
        request.setTargetCurrency("EUR");

        when(exchangeRateService.getExchangeRate("USD", "EUR"))
                .thenThrow(new ExchangeRateException("External service unavailable"));

        mockMvc.perform(get("/api/v1/exchange/rates/single")
                        .param("sourceCurrency", "USD")
                        .param("targetCurrency", "EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Exchange rate service error: External service unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getAllRates_WhenServiceThrowsException_ReturnsServiceUnavailable() throws Exception {
        when(exchangeRateService.getAllRates("USD"))
                .thenThrow(new ExchangeRateException("Failed to fetch rates"));

        mockMvc.perform(get("/api/v1/exchange/rates/all")
                        .param("currency", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Exchange rate service error: Failed to fetch rates"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

