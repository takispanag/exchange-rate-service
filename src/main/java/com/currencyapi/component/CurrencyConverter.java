package com.currencyapi.component;

import com.currencyapi.dto.ConversionResultDto;
import com.currencyapi.dto.ExchangeRateDto;
import com.currencyapi.dto.MultiConversionResultDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CurrencyConverter {

    public ConversionResultDto createSingleConversion(
            ExchangeRateDto exchangeRate,
            String fromCurrency,
            String toCurrency,
            double amount) {

        double rate = exchangeRate.getRates().get(toCurrency);

        return ConversionResultDto.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .convertedAmount(amount * rate)
                .rate(rate)
                .timestamp(Instant.now())
                .build();
    }


    // TODO the logic is not perfect for this multiconversion, to be revisited
    public MultiConversionResultDto createMultiConversion(
            ExchangeRateDto exchangeRate,
            String fromCurrency,
            List<String> targetCurrencies,
            double amount) {

        Map<String, Double> rates = exchangeRate.getRates();
        Map<String, Double> conversions = targetCurrencies.stream()
                .collect(Collectors.toMap(
                        currency -> currency,
                        currency -> rates.get(currency) * amount
                ));

        return MultiConversionResultDto.builder()
                .fromCurrency(fromCurrency)
                .amount(amount)
                .conversions(conversions)
                .rates(rates)
                .timestamp(Instant.now())
                .build();
    }
}

