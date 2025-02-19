package com.exchange.converter;

import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.ExchangeRateDto;
import com.exchange.dto.MultiConversionResultDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CurrencyConverter {

    public ConversionResultDto createSingleConversion(ExchangeRateDto exchangeRate,
                                                      String sourceCurrency,
                                                      String targetCurrency,
                                                      double amount) {

        double rate = exchangeRate.getRates().get(targetCurrency);

        return ConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .targetCurrency(targetCurrency)
                .amount(amount)
                .convertedAmount(amount * rate)
                .rate(rate)
                .timestamp(Instant.now())
                .build();
    }

    // TODO the logic is not perfect for this multiconversion, to be revisited
    public MultiConversionResultDto createMultiConversion(ExchangeRateDto exchangeRate,
                                                          String sourceCurrency,
                                                          List<String> targetCurrencies,
                                                          double amount) {

        Map<String, Double> rates = exchangeRate.getRates();
        Map<String, Double> conversions = targetCurrencies.stream()
                .collect(Collectors.toMap(
                        currency -> currency,
                        currency -> rates.get(currency) * amount
                ));

        return MultiConversionResultDto.builder()
                .sourceCurrency(sourceCurrency)
                .amount(amount)
                .conversions(conversions)
                .rates(rates)
                .timestamp(Instant.now())
                .build();
    }
}

