package com.exchange.service;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.exception.ExchangeRateException;
import com.exchange.model.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Implementation of {@link ExchangeRateProviderService} that fetches exchange rates from an external API.
 * Uses Spring's caching mechanism to optimize performance and reduce API calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateProviderServiceImpl implements ExchangeRateProviderService {
    private final RestClient exchangeClient;

    /**
     * Fetches and caches the exchange rate for a specific currency pair.
     *
     * @param sourceCurrency the source currency code
     * @param targetCurrency the target currency code
     * @return {@link SingleExchangeRateDto} containing the exchange rate information
     * @throws ExchangeRateException if the exchange rate cannot be fetched
     */
    @Override
    @Cacheable(value = "exchangeRates", key = "#sourceCurrency + '-' + #targetCurrency")
    public SingleExchangeRateDto getExchangeRate(String sourceCurrency, String targetCurrency) {
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
            throw new ExchangeRateException("Failed to fetch exchange rate from " + sourceCurrency + " to " + targetCurrency);
        }

        String rateKey = sourceCurrency + targetCurrency;

        return SingleExchangeRateDto.builder()
                .sourceCurrency(sourceCurrency)
                .targetCurrency(targetCurrency)
                .exchangeRate(response.getQuotes().get(rateKey))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Fetches and caches all exchange rates for a given base currency.
     *
     * @param currency the base currency code
     * @return {@link AllExchangeRatesDto} containing all available exchange rates
     * @throws ExchangeRateException if the exchange rates cannot be fetched
     */
    @Override
    @Cacheable(value = "allRates", key = "#currency")
    public AllExchangeRatesDto getAllRates(String currency) {
        log.info("Fetching all rates for base currency: {}", currency);

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

        return AllExchangeRatesDto.builder()
                .sourceCurrency(currency)
                .rates(response.getQuotes())
                .timestamp(Instant.now())
                .build();
    }
}
