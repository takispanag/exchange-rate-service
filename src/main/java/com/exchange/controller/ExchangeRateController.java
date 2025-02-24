package com.exchange.controller;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.model.ApiErrorResponse;
import com.exchange.model.CurrencyExchangeRequest;
import com.exchange.model.GetAllRatesRequest;
import com.exchange.service.ExchangeRateProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/exchange/rates")
@Tag(name = "Exchange Rates", description = "API for retrieving currency exchange rates")
public class ExchangeRateController {
    private final ExchangeRateProviderService exchangeRateService;

    @Operation(
            summary = "Get exchange rate between two currencies",
            description = "Retrieves the current exchange rate from source currency to target currency"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Exchange rate retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SingleExchangeRateDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid currency codes",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching exchange rate",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/single")
    public ResponseEntity<SingleExchangeRateDto> getExchangeRate(
            @Parameter(description = "Exchange rate request details", required = true)
            @Valid CurrencyExchangeRequest request) {
        return ResponseEntity.ok(exchangeRateService.getExchangeRate(
                request.getSourceCurrency(),
                request.getTargetCurrency()));
    }

    @Operation(
            summary = "Get all exchange rates for a specific currency",
            description = "Retrieves current exchange rates from the base currency to all available currencies"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Exchange rates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AllExchangeRatesDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid currency code",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching exchange rates",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/all")
    public ResponseEntity<AllExchangeRatesDto> getAllRates(
            @Parameter(description = "Get all rates request details", required = true)
            @Valid GetAllRatesRequest request) {
        return ResponseEntity.ok(exchangeRateService.getAllRates(request.getCurrency()));
    }
}
