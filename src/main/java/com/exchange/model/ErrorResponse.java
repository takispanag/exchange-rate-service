package com.exchange.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private final int status;
    private final String message;
    private final Map<String, String> errors;
    private final Instant timestamp;
}
