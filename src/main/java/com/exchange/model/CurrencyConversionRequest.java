package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CurrencyConversionRequest {

    @ValidCurrency
    @NotNull(message = "Source currency cannot be null")
    @NotBlank(message = "Source currency is required")
    private String sourceCurrency;

    @ValidCurrency
    @NotNull(message = "Target currency cannot be null")
    @NotBlank(message = "Target currency is required")
    private String targetCurrency;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
}
