package com.exchange.service;

import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.ExchangeRateDto;
import com.exchange.dto.MultiConversionResultDto;

import java.util.List;

public interface ExchangeRateService {
    ExchangeRateDto getExchangeRate(String sourceCurrency, String targetCurrency);

    ExchangeRateDto getAllRates(String currency);

    ConversionResultDto convertAmount(String sourceCurrency, String targetCurrency, double amount);

    MultiConversionResultDto convertToMultipleCurrencies(String sourceCurrency,
                                                         List<String> targetCurrencies,
                                                         double amount);
}
