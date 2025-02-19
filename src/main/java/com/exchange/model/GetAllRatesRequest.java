package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetAllRatesRequest {
    @ValidCurrency
    @NotNull(message = "Currency cannot be null")
    @NotBlank(message = "Currency is required")
    private String currency;
}
