package com.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Exchange rate between two currencies")
public class SingleExchangeRateDto {
    @Schema(description = "Source currency code", example = "USD")
    private String sourceCurrency;

    @Schema(description = "Target currency code", example = "EUR")
    private String targetCurrency;

    @Schema(description = "Exchange rate value", example = "0.92")
    private BigDecimal exchangeRate;

    @Schema(description = "Timestamp of the rate", example = "2024-02-20T13:45:30.000Z")
    private Instant timestamp;
}
