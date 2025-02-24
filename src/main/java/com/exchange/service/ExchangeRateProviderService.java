package com.exchange.service;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.SingleExchangeRateDto;

public interface ExchangeRateProviderService {
    SingleExchangeRateDto getExchangeRate(String sourceCurrency, String targetCurrency);

    AllExchangeRatesDto getAllRates(String currency);
}
