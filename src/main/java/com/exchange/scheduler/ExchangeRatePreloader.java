package com.exchange.scheduler;

import com.exchange.dto.ExchangeRateDto;
import com.exchange.service.ExchangeRateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRatePreloader {
    private final ExchangeRateService exchangeRateService;

    @Value("${exchange.rate.preload.currencies}")
    private List<String> baseCurrencies;

    @PostConstruct
    public void preloadOnStartup() {
        log.info("Starting initial preload of exchange rates");
        preloadExchangeRates();
    }

    @Async
    @Scheduled(fixedRateString = "${exchange.rate.cache.refresh-interval:50000}")
    public void preloadExchangeRates() {
        log.info("Starting scheduled preload of exchange rates for currencies: {}", baseCurrencies);

        baseCurrencies.forEach(currency -> {
            try {
                ExchangeRateDto rates = exchangeRateService.getAllRates(currency);
                log.info("Successfully preloaded rates for {}, timestamp: {}",
                        currency, rates.getTimestamp());
            } catch (Exception e) {
                log.error("Failed to preload rates for {}: {}", currency, e.getMessage());
            }
        });
    }
}