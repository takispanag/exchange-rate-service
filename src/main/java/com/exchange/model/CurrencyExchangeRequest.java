package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for getting exchange rate between two currencies")
public class CurrencyExchangeRequest {
    @ValidCurrency
    @NotNull(message = "Source currency cannot be null")
    @NotBlank(message = "Source currency is required")
    @Schema(description = "Source currency code", example = "USD")
    private String sourceCurrency;

    @ValidCurrency
    @NotNull(message = "Target currency cannot be null")
    @NotBlank(message = "Target currency is required")
    @Schema(description = "Target currency code", example = "EUR")
    private String targetCurrency;
}