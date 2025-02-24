package com.exchange.service;

import com.exchange.converter.CurrencyConverter;
import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.dto.SingleExchangeRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of {@link CurrencyConversionService} that handles currency conversion operations.
 * This service coordinates between {@link ExchangeRateProviderService} for fetching rates and
 * {@link CurrencyConverter} for performing the actual conversions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConversionServiceImpl implements CurrencyConversionService {
    private final ExchangeRateProviderService exchangeProvider;
    private final CurrencyConverter currencyConverter;

    /**
     * Converts an amount from one currency to another.
     *
     * @param sourceCurrency the source currency code
     * @param targetCurrency the target currency code
     * @param amount         the amount to convert
     * @return ConversionResultDto containing the converted amount and rate details
     */
    @Override
    public ConversionResultDto convertAmount(String sourceCurrency, String targetCurrency, BigDecimal amount) {
        SingleExchangeRateDto exchangeRate = exchangeProvider.getExchangeRate(sourceCurrency, targetCurrency);
        return currencyConverter.createSingleConversion(exchangeRate, sourceCurrency, targetCurrency, amount);
    }

    /**
     * Converts an amount from one currency to multiple target currencies simultaneously.
     *
     * @param sourceCurrency   the source currency code
     * @param targetCurrencies list of target currency codes
     * @param amount           the amount to convert
     * @return MultiConversionResultDto containing all conversion results
     */
    @Override
    public MultiConversionResultDto convertToMultipleCurrencies(String sourceCurrency,
                                                                List<String> targetCurrencies,
                                                                BigDecimal amount) {
        AllExchangeRatesDto exchangeRate = exchangeProvider.getAllRates(sourceCurrency);
        return currencyConverter.createMultiConversion(exchangeRate, sourceCurrency, targetCurrencies, amount);
    }
}