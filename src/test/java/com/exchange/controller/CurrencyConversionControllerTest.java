package com.exchange.controller;

import com.exchange.config.CurrencyConfig;
import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.exception.CurrencyConversionException;
import com.exchange.service.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyConversionController.class)
class CurrencyConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyConversionService currencyConversionService;

    @MockitoBean
    private CurrencyConfig currencyConfig;

    private ConversionResultDto singleConversionResult;
    private MultiConversionResultDto multiConversionResult;

    private static final String VALID_MULTI_CONVERSION_REQUEST = """
            {
                "sourceCurrency": "USD",
                "targetCurrencies": ["EUR", "GBP"],
                "amount": 100
            }
            """;

    private static final String INVALID_MULTI_CONVERSION_REQUEST = """
            {
                "sourceCurrency": "",
                "targetCurrencies": ["EUR", "GBP"],
                "amount": 100
            }
            """;

    private static final String MALFORMED_JSON_REQUEST = """
            {
                "sourceCurrency": "USD",
                "targetCurrencies": ["EUR", "GBP",]
                "amount": 100
            }
            """;

    @BeforeEach
    void setUp() {
        when(currencyConfig.getSupported())
                .thenReturn(Set.of("USD", "EUR", "GBP"));

        singleConversionResult = ConversionResultDto.builder()
                .sourceCurrency("USD")
                .targetCurrency("EUR")
                .sourceAmount(BigDecimal.valueOf(100))
                .convertedAmount(BigDecimal.valueOf(85))
                .exchangeRate(BigDecimal.valueOf(0.85))
                .build();

        multiConversionResult = MultiConversionResultDto.builder()
                .sourceCurrency("USD")
                .sourceAmount(BigDecimal.valueOf(100))
                .conversions(Map.of(
                        "EUR", BigDecimal.valueOf(85),
                        "GBP", BigDecimal.valueOf(73)
                ))
                .build();
    }

    @Test
    void convertValue_ValidRequest_ReturnsConversionResult() throws Exception {
        when(currencyConversionService.convertAmount(any(), any(), any()))
                .thenReturn(singleConversionResult);

        mockMvc.perform(get("/api/v1/exchange/convert/single")
                        .param("sourceCurrency", "USD")
                        .param("targetCurrency", "EUR")
                        .param("amount", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.sourceAmount").value(100))
                .andExpect(jsonPath("$.convertedAmount").value(85))
                .andExpect(jsonPath("$.exchangeRate").value(0.85));
    }

    @Test
    void convertValue_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/convert/single")
                        .param("sourceCurrency", "")
                        .param("targetCurrency", "EUR")
                        .param("amount", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void convertToMultipleCurrencies_ValidRequest_ReturnsMultiConversionResult() throws Exception {
        when(currencyConversionService.convertToMultipleCurrencies(any(), anyList(), any()))
                .thenReturn(multiConversionResult);

        mockMvc.perform(post("/api/v1/exchange/convert/multiple")
                        .content(VALID_MULTI_CONVERSION_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.sourceAmount").value(100))
                .andExpect(jsonPath("$.conversions.EUR").value(85))
                .andExpect(jsonPath("$.conversions.GBP").value(73));
    }

    @Test
    void convertToMultipleCurrencies_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/exchange/convert/multiple")
                        .content(INVALID_MULTI_CONVERSION_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void convertValue_WhenCurrencyConversionException_ReturnsBadRequest() throws Exception {
        when(currencyConversionService.convertAmount(any(), any(), any()))
                .thenThrow(new CurrencyConversionException("Test conversion error"));

        mockMvc.perform(get("/api/v1/exchange/convert/single")
                        .param("sourceCurrency", "USD")
                        .param("targetCurrency", "EUR")
                        .param("amount", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Currency conversion failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void convertValue_WhenConstraintViolation_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/convert/single")
                        .param("sourceCurrency", "INVALID")
                        .param("targetCurrency", "EUR")
                        .param("amount", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void whenMalformedJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/exchange/convert/multiple")
                        .content(MALFORMED_JSON_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid request body format")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
