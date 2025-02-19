package com.exchange.model;


import com.exchange.validator.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CurrencyExchangeRequest {
    @ValidCurrency
    @NotNull(message = "Source currency cannot be null")
    @NotBlank(message = "Source currency is required")
    private String sourceCurrency;

    @ValidCurrency
    @NotNull(message = "Target currency cannot be null")
    @NotBlank(message = "Target currency is required")
    private String targetCurrency;
}