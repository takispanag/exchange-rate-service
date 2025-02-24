package com.exchange.service;

import com.exchange.converter.CurrencyConverter;
import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.exception.CurrencyConversionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceImplTest {

    @Mock
    private ExchangeRateProviderService exchangeProvider;

    @Mock
    private CurrencyConverter currencyConverter;

    @InjectMocks
    private CurrencyConversionServiceImpl currencyConversionService;

    private static Stream<Arguments> provideEdgeCaseAmounts() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO, "Zero amount"),
                Arguments.of(BigDecimal.valueOf(0.000001), "Very small amount"),
                Arguments.of(BigDecimal.valueOf(999999999.999999), "Very large amount"),
                Arguments.of(BigDecimal.valueOf(-100), "Negative amount"),
                Arguments.of(new BigDecimal("0.1234567890"), "Many decimal places")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("provideEdgeCaseAmounts")
    void convertAmount_EdgeCaseAmounts(BigDecimal amount, String testCase) {
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        SingleExchangeRateDto exchangeRate = SingleExchangeRateDto.builder()
                .exchangeRate(BigDecimal.valueOf(0.85))
                .build();

        ConversionResultDto expectedResult = ConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .targetCurrency(targetCurrency)
                .sourceAmount(amount)
                .convertedAmount(amount.multiply(exchangeRate.getExchangeRate()).setScale(6, RoundingMode.HALF_UP))
                .timestamp(Instant.now())
                .build();

        when(exchangeProvider.getExchangeRate(sourceCurrency, targetCurrency)).thenReturn(exchangeRate);
        when(currencyConverter.createSingleConversion(exchangeRate, sourceCurrency, targetCurrency, amount))
                .thenReturn(expectedResult);

        ConversionResultDto result = currencyConversionService.convertAmount(sourceCurrency, targetCurrency, amount);
        assertEquals(expectedResult, result);
    }

    @Test
    void convertAmount_WhenExchangeProviderThrowsException() {
        when(exchangeProvider.getExchangeRate("USD", "XXX"))
                .thenThrow(new CurrencyConversionException("Invalid currency"));

        assertThrows(CurrencyConversionException.class,
                () -> currencyConversionService.convertAmount("USD", "XXX", BigDecimal.ONE));
    }

    @Test
    void convertToMultipleCurrencies_WithEmptyTargetList() {
        String sourceCurrency = "USD";
        List<String> targetCurrencies = Collections.emptyList();
        BigDecimal amount = BigDecimal.ONE;

        AllExchangeRatesDto exchangeRates = AllExchangeRatesDto.builder()
                .rates(new HashMap<>())
                .build();

        MultiConversionResultDto expectedResult = MultiConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .sourceAmount(amount)
                .conversions(Collections.emptyMap())
                .timestamp(Instant.now())
                .build();

        when(exchangeProvider.getAllRates(sourceCurrency)).thenReturn(exchangeRates);
        when(currencyConverter.createMultiConversion(exchangeRates, sourceCurrency, targetCurrencies, amount))
                .thenReturn(expectedResult);

        MultiConversionResultDto result = currencyConversionService.convertToMultipleCurrencies(
                sourceCurrency, targetCurrencies, amount);

        assertTrue(result.getConversions().isEmpty());
    }

    @Test
    void convertToMultipleCurrencies_WithDuplicateTargetCurrencies() {
        String sourceCurrency = "USD";
        List<String> targetCurrencies = Arrays.asList("EUR", "EUR", "GBP", "GBP");
        BigDecimal amount = BigDecimal.ONE;

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USDEUR", BigDecimal.valueOf(0.85));
        rates.put("USDGBP", BigDecimal.valueOf(0.73));

        AllExchangeRatesDto exchangeRates = AllExchangeRatesDto.builder()
                .rates(rates)
                .build();

        Map<String, BigDecimal> conversions = new HashMap<>();
        conversions.put("EUR", BigDecimal.valueOf(0.85));
        conversions.put("GBP", BigDecimal.valueOf(0.73));

        MultiConversionResultDto expectedResult = MultiConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .sourceAmount(amount)
                .conversions(conversions)
                .timestamp(Instant.now())
                .build();

        when(exchangeProvider.getAllRates(sourceCurrency)).thenReturn(exchangeRates);
        when(currencyConverter.createMultiConversion(exchangeRates, sourceCurrency, targetCurrencies, amount))
                .thenReturn(expectedResult);

        MultiConversionResultDto result = currencyConversionService.convertToMultipleCurrencies(
                sourceCurrency, targetCurrencies, amount);

        assertEquals(2, result.getConversions().size());
    }

    @Test
    void convertToMultipleCurrencies_WithMaximumPrecisionRates() {
        String sourceCurrency = "USD";
        List<String> targetCurrencies = Arrays.asList("EUR", "GBP");
        BigDecimal amount = new BigDecimal("1.123456789");

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USDEUR", new BigDecimal("0.8534567890"));
        rates.put("USDGBP", new BigDecimal("0.7312345678"));

        AllExchangeRatesDto exchangeRates = AllExchangeRatesDto.builder()
                .rates(rates)
                .build();

        when(exchangeProvider.getAllRates(sourceCurrency)).thenReturn(exchangeRates);
        when(currencyConverter.createMultiConversion(any(), any(), any(), any()))
                .thenReturn(MultiConversionResultDto.builder()
                        .sourceCurrency(sourceCurrency)
                        .sourceAmount(amount)
                        .conversions(rates)
                        .timestamp(Instant.now())
                        .build());

        MultiConversionResultDto result = currencyConversionService.convertToMultipleCurrencies(
                sourceCurrency, targetCurrencies, amount);

        assertNotNull(result);
        assertEquals(2, result.getConversions().size());
    }
}

