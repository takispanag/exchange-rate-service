package com.exchange.controller;

import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.ExchangeRateDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.model.CurrencyConversionRequest;
import com.exchange.model.CurrencyExchangeRequest;
import com.exchange.model.GetAllRatesRequest;
import com.exchange.model.MultiCurrencyConversionRequest;
import com.exchange.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange")
@Tag(name = "Currency Exchange API", description = "API for currency exchange operations")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/rate")
    @Operation(summary = "Get exchange rate between two currencies")
    public ResponseEntity<ExchangeRateDto> getExchangeRate(@Valid CurrencyExchangeRequest request) {

        return ResponseEntity.ok(exchangeRateService.getExchangeRate(request.getSourceCurrency(), request.getTargetCurrency()));
    }

    @GetMapping("/rates")
    @Operation(summary = "Get all exchange rates for a specific currency")
    public ResponseEntity<ExchangeRateDto> getAllRates(@Valid GetAllRatesRequest request) {

        return ResponseEntity.ok(exchangeRateService.getAllRates(request.getCurrency()));
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert amount from one currency to another")
    public ResponseEntity<ConversionResultDto> convertValue(
            @Valid CurrencyConversionRequest request) {

        return ResponseEntity.ok(exchangeRateService.convertAmount(
                request.getSourceCurrency(),
                request.getTargetCurrency(),
                request.getAmount()));
    }

    @GetMapping("/convert-multiple")
    @Operation(summary = "Convert amount from one currency to multiple currencies")
    public ResponseEntity<MultiConversionResultDto> convertToMultipleCurrencies(
            @Valid MultiCurrencyConversionRequest request) {

        return ResponseEntity.ok(exchangeRateService.convertToMultipleCurrencies(
                request.getSourceCurrency(),
                request.getTargetCurrencies(),
                request.getAmount()));
    }
}
