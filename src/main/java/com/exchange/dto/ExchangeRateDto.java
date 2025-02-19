package com.exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class ExchangeRateDto {
    private String sourceCurrency;
    private Map<String, Double> rates;
    private Instant timestamp;
}
