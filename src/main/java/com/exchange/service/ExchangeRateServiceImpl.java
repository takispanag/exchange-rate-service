package com.exchange.service;

import com.exchange.converter.CurrencyConverter;
import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.ExchangeRateDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.exception.ExchangeRateException;
import com.exchange.model.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final RestClient exchangeClient;
    private final CurrencyConverter currencyConverter;

    @Override
    @Cacheable(value = "exchangeRates", key = "#sourceCurrency + '-' + #targetCurrency")
    public ExchangeRateDto getExchangeRate(String sourceCurrency, String targetCurrency) {
        log.info("Fetching exchange rate from {} to {}", sourceCurrency, targetCurrency);

        ExchangeRateResponse response = exchangeClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/live")
                        .queryParam("source", sourceCurrency)
                        .queryParam("currencies", targetCurrency)
                        .build())
                .retrieve()
                .body(ExchangeRateResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new ExchangeRateException("Failed to fetch exchange rate from" + sourceCurrency + " to " + targetCurrency);
        }

        return ExchangeRateDto.builder()
                .sourceCurrency(sourceCurrency)
                .rates(response.getQuotes())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    @Cacheable(value = "allRates", key = "#currency", sync = true)
    public ExchangeRateDto getAllRates(String currency) {
        log.info("Cache miss - Fetching all rates for base currency: {}", currency);

        ExchangeRateResponse response = exchangeClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/live")
                        .queryParam("source", currency)
                        .build())
                .retrieve()
                .body(ExchangeRateResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new ExchangeRateException("Failed to fetch exchange rates for " + currency);
        }

        return ExchangeRateDto.builder()
                .sourceCurrency(currency)
                .rates(response.getQuotes())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public ConversionResultDto convertAmount(String sourceCurrency, String targetCurrency, double amount) {
        ExchangeRateDto exchangeRate = getExchangeRate(sourceCurrency, targetCurrency);
        return currencyConverter.createSingleConversion(exchangeRate, sourceCurrency, targetCurrency, amount);
    }

    @Override
    public MultiConversionResultDto convertToMultipleCurrencies(String sourceCurrency,
                                                                List<String> targetCurrencies,
                                                                double amount) {
        ExchangeRateDto exchangeRate = getAllRates(sourceCurrency);
        return currencyConverter.createMultiConversion(exchangeRate, sourceCurrency, targetCurrencies, amount);
    }
}