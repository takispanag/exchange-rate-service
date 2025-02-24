package com.exchange.preload;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.service.ExchangeRateProviderService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRatePreloader implements ApplicationListener<ContextRefreshedEvent> {
    private final ExchangeRateProviderService exchangeProviderService;

    @Value("${exchange.rate.preload.currencies}")
    private List<String> baseCurrencies;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        loadExchangeRates();
    }

    @Scheduled(fixedRateString = "${exchange.rate.cache.refresh-interval-ms:51000}", initialDelay = 51000)
    public void loadExchangeRates() {
        log.info("Starting preload of exchange rates for currencies: {}", baseCurrencies);

        baseCurrencies.forEach(currency -> {
            try {
                AllExchangeRatesDto rates = exchangeProviderService.getAllRates(currency);
                log.info("Successfully preloaded rates for {}, timestamp: {}",
                        currency, rates.getTimestamp());
            } catch (Exception e) {
                log.error("Failed to preload rates for {}: {}", currency, e.getMessage());
            }
        });
    }
}