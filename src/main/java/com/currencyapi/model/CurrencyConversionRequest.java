package com.currencyapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CurrencyConversionRequest {
    @NotBlank(message = "Source currency is required")
    private String fromCurrency;

    @NotBlank(message = "Target currency is required")
    private String toCurrency;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
}
