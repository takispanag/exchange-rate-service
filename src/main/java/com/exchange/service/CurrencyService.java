package com.exchange.service;

import com.exchange.dto.CurrencyDto;
import com.exchange.exception.ExchangeRateException;
import com.exchange.model.CurrencyListResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableRetry
public class CurrencyService {

    private final RestClient exchangeClient;

    @Getter
    private List<CurrencyDto> availableCurrencies;

    @PostConstruct
    public void init() {
        loadCurrencies();
    }

    @Retryable(
            retryFor = {Exception.class},
            backoff = @Backoff(delay = 5000)
    )
    public void loadCurrencies() {
        log.info("Loading available currencies on startup");
        try {
            CurrencyListResponse response = exchangeClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/list")
                            .build())
                    .retrieve()
                    .body(CurrencyListResponse.class);

            if (response == null || !response.isSuccess()) {
                throw new ExchangeRateException("Failed to fetch available currencies");
            }

            availableCurrencies = response.getCurrencies().entrySet().stream()
                    .map(entry -> CurrencyDto.builder()
                            .code(entry.getKey())
                            .name(entry.getValue())
                            .build()).toList();

            log.info("Loaded {} currencies", availableCurrencies.size());
        } catch (Exception e) {
            log.error("Failed to load currencies on startup: {}", e.getMessage());
        }
    }

    @Recover
    private void fallback(Exception e) {
        log.error("All retries failed to load currencies: {}", e.getMessage());
        availableCurrencies = List.of(
                CurrencyDto.builder().code("USD").name("US Dollar").build(),
                CurrencyDto.builder().code("EUR").name("Euro").build(),
                CurrencyDto.builder().code("GBP").name("British Pound").build(),
                CurrencyDto.builder().code("JPY").name("Japanese Yen").build()
        );
        log.info("Loaded fallback currencies");
    }
}