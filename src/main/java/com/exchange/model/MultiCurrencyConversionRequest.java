package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class MultiCurrencyConversionRequest {

    @ValidCurrency
    @NotNull(message = "Source currency cannot be null")
    @NotBlank(message = "Source currency is required")
    private String sourceCurrency;

    @NotEmpty(message = "Target currencies list cannot be empty")
    private List<String> targetCurrencies;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
}
