package com.exchange.preload;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.service.ExchangeRateProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRatePreloaderTest {

    @Mock
    private ExchangeRateProviderService exchangeProviderService;

    @InjectMocks
    private ExchangeRatePreloader exchangeRatePreloader;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRatePreloader, "baseCurrencies", Arrays.asList("USD", "EUR", "GBP"));
    }

    @Test
    void loadExchangeRates_SuccessfulPreload() {
        AllExchangeRatesDto mockRates = AllExchangeRatesDto.builder()
                .timestamp(Instant.now())
                .rates(new HashMap<>())
                .build();

        when(exchangeProviderService.getAllRates(anyString())).thenReturn(mockRates);

        exchangeRatePreloader.loadExchangeRates();

        verify(exchangeProviderService, times(3)).getAllRates(anyString());
    }

    @Test
    void loadExchangeRates_HandlesException() {
        when(exchangeProviderService.getAllRates(anyString())).thenThrow(new RuntimeException("API Error"));

        exchangeRatePreloader.loadExchangeRates();

        verify(exchangeProviderService, times(3)).getAllRates(anyString());
    }

    @Test
    void onApplicationEvent_InitiatesLoadExchangeRates() {
        ContextRefreshedEvent mockEvent = mock(ContextRefreshedEvent.class);

        exchangeRatePreloader.onApplicationEvent(mockEvent);

        verify(exchangeProviderService, times(3)).getAllRates(anyString());
    }
}
