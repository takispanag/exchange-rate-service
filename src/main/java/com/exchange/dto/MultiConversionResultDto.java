package com.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Schema(description = "Result of a multi-currency conversion operation")
public class MultiConversionResultDto {
    @Schema(description = "Source currency code", example = "USD")
    private String sourceCurrency;

    @Schema(description = "Original amount to convert", example = "100.00")
    private BigDecimal sourceAmount;

    @Schema(description = "Map of target currencies to their converted amounts",
            example = "{\"EUR\": 92.47, \"GBP\": 79.32, \"JPY\": 14950.00}")
    private Map<String, BigDecimal> conversions;

    @Schema(description = "Timestamp of the conversion", example = "2024-02-20T13:45:30.000Z")
    private Instant timestamp;
}
