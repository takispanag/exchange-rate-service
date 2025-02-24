package com.exchange.converter;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.exception.CurrencyConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {

    private CurrencyConverter currencyConverter;

    @BeforeEach
    void setUp() {
        currencyConverter = new CurrencyConverter();
    }

    @Test
    void createSingleConversion_Success() {
        SingleExchangeRateDto exchangeRate = SingleExchangeRateDto.builder()
                .exchangeRate(BigDecimal.valueOf(1.5))
                .build();
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);

        ConversionResultDto result = currencyConverter.createSingleConversion(
                exchangeRate, sourceCurrency, targetCurrency, amount);

        assertNotNull(result);
        assertEquals(sourceCurrency, result.getSourceCurrency());
        assertEquals(targetCurrency, result.getTargetCurrency());
        assertEquals(amount, result.getSourceAmount());
        assertEquals(BigDecimal.valueOf(150).setScale(6, RoundingMode.HALF_UP), result.getConvertedAmount());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void createMultiConversion_Success() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USDEUR", BigDecimal.valueOf(0.85));
        rates.put("USDGBP", BigDecimal.valueOf(0.73));

        AllExchangeRatesDto exchangeRates = AllExchangeRatesDto.builder()
                .rates(rates)
                .build();

        String sourceCurrency = "USD";
        List<String> targetCurrencies = Arrays.asList("EUR", "GBP");
        BigDecimal amount = BigDecimal.valueOf(100);

        MultiConversionResultDto result = currencyConverter.createMultiConversion(
                exchangeRates, sourceCurrency, targetCurrencies, amount);

        assertNotNull(result);
        assertEquals(sourceCurrency, result.getSourceCurrency());
        assertEquals(amount, result.getSourceAmount());
        assertNotNull(result.getTimestamp());

        Map<String, BigDecimal> conversions = result.getConversions();
        assertEquals(BigDecimal.valueOf(85).setScale(6, RoundingMode.HALF_UP), conversions.get("EUR"));
        assertEquals(BigDecimal.valueOf(73).setScale(6, RoundingMode.HALF_UP), conversions.get("GBP"));
    }

    @Test
    void createMultiConversion_ThrowsException_WhenRateNotFound() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USDEUR", BigDecimal.valueOf(0.85));

        AllExchangeRatesDto exchangeRates = AllExchangeRatesDto.builder()
                .rates(rates)
                .build();

        String sourceCurrency = "USD";
        List<String> targetCurrencies = Arrays.asList("EUR", "JPY");
        BigDecimal amount = BigDecimal.valueOf(100);

        CurrencyConversionException exception = assertThrows(
                CurrencyConversionException.class,
                () -> currencyConverter.createMultiConversion(
                        exchangeRates, sourceCurrency, targetCurrencies, amount)
        );

        assertEquals("Exchange rate not found for pair USD-JPY", exception.getMessage());
    }

    @Test
    void createSingleConversion_HandlesZeroAmount() {
        SingleExchangeRateDto exchangeRate = SingleExchangeRateDto.builder()
                .exchangeRate(BigDecimal.valueOf(1.5))
                .build();
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        BigDecimal amount = BigDecimal.ZERO;

        ConversionResultDto result = currencyConverter.createSingleConversion(
                exchangeRate, sourceCurrency, targetCurrency, amount);

        assertEquals(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP), result.getConvertedAmount());
    }
}
