package com.exchange.converter;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.exception.CurrencyConversionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Component responsible for performing currency conversion calculations and creating conversion result objects.
 * Handles both single currency and multiple currency conversion scenarios.
 */
@Component
public class CurrencyConverter {

    /**
     * Creates a single currency conversion result.
     *
     * @param exchangeRateDto the exchange rate data transfer object containing the conversion rate
     * @param sourceCurrency  the source currency code
     * @param targetCurrency  the target currency code
     * @param amount          the amount to convert
     * @return {@link ConversionResultDto} containing the conversion details
     */
    public ConversionResultDto createSingleConversion(SingleExchangeRateDto exchangeRateDto,
                                                      String sourceCurrency,
                                                      String targetCurrency,
                                                      BigDecimal amount) {
        BigDecimal convertedAmount = amount.multiply(exchangeRateDto.getExchangeRate()).setScale(6, RoundingMode.HALF_UP);

        return ConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .targetCurrency(targetCurrency)
                .sourceAmount(amount)
                .convertedAmount(convertedAmount)
                .exchangeRate(exchangeRateDto.getExchangeRate())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates multiple currency conversion results.
     *
     * @param exchangeRate     the {@link AllExchangeRatesDto} containing all available exchange rates
     * @param sourceCurrency   the source currency code
     * @param targetCurrencies list of target currency codes
     * @param amount           the amount to convert
     * @return {@link MultiConversionResultDto} containing all conversion results
     */
    public MultiConversionResultDto createMultiConversion(
            AllExchangeRatesDto exchangeRate,
            String sourceCurrency,
            List<String> targetCurrencies,
            BigDecimal amount) {

        Map<String, BigDecimal> conversions = convertToAllTargetCurrencies(
                exchangeRate.getRates(),
                sourceCurrency,
                targetCurrencies,
                amount
        );

        return buildConversionResult(sourceCurrency, amount, conversions);
    }

    private Map<String, BigDecimal> convertToAllTargetCurrencies(
            Map<String, BigDecimal> rates,
            String sourceCurrency,
            List<String> targetCurrencies,
            BigDecimal amount) {

        return targetCurrencies.stream()
                .collect(Collectors.toMap(
                        currency -> currency,
                        currency -> convertSingleCurrency(rates, sourceCurrency, currency, amount)
                ));
    }

    private BigDecimal convertSingleCurrency(
            Map<String, BigDecimal> rates,
            String sourceCurrency,
            String targetCurrency,
            BigDecimal amount) {

        String rateKey = sourceCurrency + targetCurrency;
        BigDecimal rate = Optional.ofNullable(rates.get(rateKey))
                .orElseThrow(() -> createNotFoundException(sourceCurrency, targetCurrency));

        return rate.multiply(amount)
                .setScale(6, RoundingMode.HALF_UP);
    }

    private CurrencyConversionException createNotFoundException(String sourceCurrency, String targetCurrency) {
        String message = String.format("Exchange rate not found for pair %s-%s", sourceCurrency, targetCurrency);
        return new CurrencyConversionException(message);
    }

    private MultiConversionResultDto buildConversionResult(
            String sourceCurrency,
            BigDecimal amount,
            Map<String, BigDecimal> conversions) {

        return MultiConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .sourceAmount(amount)
                .conversions(conversions)
                .timestamp(Instant.now())
                .build();
    }
}

