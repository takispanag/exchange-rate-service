package com.exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class MultiConversionResultDto {
    private String sourceCurrency;
    private double amount;
    private Map<String, Double> conversions;
    private Map<String, Double> rates;
    private Instant timestamp;
}
