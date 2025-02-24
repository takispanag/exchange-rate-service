package com.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Schema(description = "All exchange rates for a base currency")
public class AllExchangeRatesDto {
    @Schema(description = "Base currency code", example = "USD")
    private String sourceCurrency;

    @Schema(description = "Map of currency codes to their exchange rates")
    private Map<String, BigDecimal> rates;

    @Schema(description = "Timestamp of the rates", example = "2024-02-20T13:45:30.000Z")
    private Instant timestamp;
}
