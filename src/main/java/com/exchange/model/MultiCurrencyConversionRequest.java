package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Multi-currency conversion request")
public class MultiCurrencyConversionRequest {

    @ValidCurrency
    @NotNull(message = "Source currency cannot be null")
    @NotBlank(message = "Source currency is required")
    @Schema(description = "Source currency code", example = "USD")
    private String sourceCurrency;

    @NotEmpty(message = "Target currencies list cannot be empty")
    @Schema(description = "List of target currency codes", example = "[\"EUR\", \"GBP\", \"JPY\"]")
    private List<String> targetCurrencies;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @Schema(description = "Amount to convert", example = "100.00")
    private BigDecimal amount;
}
