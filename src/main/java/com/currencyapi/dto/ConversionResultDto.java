package com.currencyapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConversionResultDto {
    private String fromCurrency;
    private String toCurrency;
    private double amount;
    private double convertedAmount;
    private double rate;
    private Instant timestamp;
}
