package com.currencyapi.controller;

import com.currencyapi.dto.ConversionResultDto;
import com.currencyapi.dto.ExchangeRateDto;
import com.currencyapi.dto.MultiConversionResultDto;
import com.currencyapi.model.CurrencyConversionRequest;
import com.currencyapi.model.MultiCurrencyConversionRequest;
import com.currencyapi.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange")
@Tag(name = "Currency Exchange API", description = "API for currency exchange operations")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;


    //TODO maybe preload valid currencies from external service and create custom annotation for validation?

    @GetMapping("/rate")
    @Operation(summary = "Get exchange rate between two currencies")
    public ResponseEntity<ExchangeRateDto> getExchangeRate(
            @NotBlank(message = "Source currency is required") @RequestParam String from,
            @NotBlank(message = "Target currency is required") @RequestParam String to) {

        return ResponseEntity.ok(exchangeRateService.getExchangeRate(from, to));
    }

    @GetMapping("/rates")
    @Operation(summary = "Get all exchange rates for a specific currency")
    public ResponseEntity<ExchangeRateDto> getAllRates(
            @NotBlank(message = "Currency is required") @RequestParam String currency) {

        return ResponseEntity.ok(exchangeRateService.getAllRates(currency));
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert amount from one currency to another")
    public ResponseEntity<ConversionResultDto> convertValue(
            @Valid @RequestBody CurrencyConversionRequest request) {

        return ResponseEntity.ok(exchangeRateService.convertValue(
                request.getFromCurrency(),
                request.getToCurrency(),
                request.getAmount()));
    }

    @PostMapping("/convert-multiple")
    @Operation(summary = "Convert amount from one currency to multiple currencies")
    public ResponseEntity<MultiConversionResultDto> convertToMultipleCurrencies(
            @Valid @RequestBody MultiCurrencyConversionRequest request) {

        return ResponseEntity.ok(exchangeRateService.convertToMultipleCurrencies(
                request.getFromCurrency(),
                request.getTargetCurrencies(),
                request.getAmount()));
    }
}
