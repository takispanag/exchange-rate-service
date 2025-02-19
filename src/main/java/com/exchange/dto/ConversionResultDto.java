package com.exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConversionResultDto {
    private String sourceCurrency;
    private String targetCurrency;
    private double amount;
    private double convertedAmount;
    private double rate;
    private Instant timestamp;
}
