package com.exchange.model;

import lombok.Data;

import java.util.Map;

@Data
public class CurrencyListResponse {
    private boolean success;
    private Map<String, String> currencies;
}
