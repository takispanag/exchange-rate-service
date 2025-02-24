package com.exchange.controller;

import com.exchange.dto.ConversionResultDto;
import com.exchange.dto.MultiConversionResultDto;
import com.exchange.model.ApiErrorResponse;
import com.exchange.model.CurrencyConversionRequest;
import com.exchange.model.MultiCurrencyConversionRequest;
import com.exchange.service.CurrencyConversionService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange/convert")
@Tag(name = "Currency Conversion", description = "API for currency conversion operations")
public class CurrencyConversionController {
    private final CurrencyConversionService currencyConversionService;

    @Operation(
            summary = "Convert amount from one currency to another",
            description = "Converts a specified amount from source currency to target currency using current exchange rates"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful conversion",
                    content = @Content(schema = @Schema(implementation = ConversionResultDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input parameters",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/single")
    public ResponseEntity<ConversionResultDto> convertValue(
            @Parameter(description = "Currency conversion request details", required = true)
            @Valid CurrencyConversionRequest request) {
        return ResponseEntity.ok(currencyConversionService.convertAmount(
                request.getSourceCurrency(),
                request.getTargetCurrency(),
                request.getAmount()));
    }

    @Operation(
            summary = "Convert amount from one currency to multiple currencies",
            description = "Converts a specified amount from source currency to multiple target currencies simultaneously"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful multi-currency conversion",
                    content = @Content(schema = @Schema(implementation = MultiConversionResultDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input parameters",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/multiple")
    public ResponseEntity<MultiConversionResultDto> convertToMultipleCurrencies(
            @Parameter(description = "Multi-currency conversion request details", required = true)
            @RequestBody @Valid MultiCurrencyConversionRequest request) {
        return ResponseEntity.ok(currencyConversionService.convertToMultipleCurrencies(
                request.getSourceCurrency(),
                request.getTargetCurrencies(),
                request.getAmount()));
    }
}
