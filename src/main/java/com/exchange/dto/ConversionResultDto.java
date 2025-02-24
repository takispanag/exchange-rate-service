package com.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Result of a currency conversion operation")
public class ConversionResultDto {
    @Schema(description = "Source currency code", example = "USD")
    private String sourceCurrency;

    @Schema(description = "Target currency code", example = "EUR")
    private String targetCurrency;

    @Schema(description = "Original amount to convert", example = "100.00")
    private BigDecimal sourceAmount;

    @Schema(description = "Converted amount in target currency", example = "92.47")
    private BigDecimal convertedAmount;

    @Schema(description = "Exchange rate used for conversion", example = "0.9247")
    private BigDecimal exchangeRate;

    @Schema(description = "Timestamp of the conversion", example = "2024-02-20T13:45:30.000Z")
    private Instant timestamp;
}
