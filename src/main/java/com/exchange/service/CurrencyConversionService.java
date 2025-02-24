package com.exchange.service;

import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;

import java.math.BigDecimal;
import java.util.List;

public interface CurrencyConversionService {
    ConversionResultDto convertAmount(String sourceCurrency, String targetCurrency, BigDecimal amount);

    MultiConversionResultDto convertToMultipleCurrencies(String sourceCurrency,
                                                         List<String> targetCurrencies,
                                                         BigDecimal amount);
}
