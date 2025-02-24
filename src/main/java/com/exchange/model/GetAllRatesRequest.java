package com.exchange.model;

import com.exchange.validator.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for getting all exchange rates for a currency")
public class GetAllRatesRequest {
    @ValidCurrency
    @NotNull(message = "Currency cannot be null")
    @NotBlank(message = "Currency is required")
    @Schema(description = "Base currency code", example = "USD")
    private String currency;
}
