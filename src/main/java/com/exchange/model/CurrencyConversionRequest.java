package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Currency conversion request")
public class CurrencyConversionRequest {

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

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @Schema(description = "Amount to convert", example = "100.00")
    private BigDecimal amount;
}
