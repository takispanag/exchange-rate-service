package com.currencyapi.service;

import com.currencyapi.component.CurrencyConverter;
import com.currencyapi.dto.ConversionResultDto;
import com.currencyapi.dto.ExchangeRateDto;
import com.currencyapi.dto.MultiConversionResultDto;
import com.currencyapi.exception.ExchangeRateException;
import com.currencyapi.model.ExchangeRateResponse;
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
public class ExchangeRateService {

    private final RestClient restClient;
    private final CurrencyConverter currencyConverter;

    @Cacheable(value = "exchangeRates", key = "#from + '-' + #to")
    public ExchangeRateDto getExchangeRate(String from, String to) {
        log.info("Fetching exchange rate from {} to {}", from, to);

        ExchangeRateResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/live")
                        .queryParam("source", from)
                        .queryParam("currencies", to)
                        .build())
                .retrieve()
                .body(ExchangeRateResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new ExchangeRateException("Failed to fetch exchange rate");
        }

        return ExchangeRateDto.builder()
                .baseCurrency(from)
                .rates(response.getQuotes())
                .timestamp(Instant.ofEpochSecond(response.getTimestamp()))
                .build();
    }

    @Cacheable(value = "allRates", key = "#currency", sync = true)
    public ExchangeRateDto getAllRates(String currency) {
        log.info("Cache miss - Fetching all rates for base currency: {}", currency);

        ExchangeRateResponse response = restClient.get()
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
                .baseCurrency(currency)
                .rates(response.getQuotes())
                .timestamp(Instant.ofEpochSecond(response.getTimestamp()))
                .build();
    }

    public ConversionResultDto convertValue(
            String from,
            String to,
            double amount) {
        ExchangeRateDto exchangeRate = getExchangeRate(from, to);
        return currencyConverter.createSingleConversion(exchangeRate, from, to, amount);
    }

    public MultiConversionResultDto convertToMultipleCurrencies(
            String from,
            List<String> toCurrencies,
            double amount) {
        ExchangeRateDto exchangeRate = getAllRates(from);
        return currencyConverter.createMultiConversion(exchangeRate, from, toCurrencies, amount);
    }
}