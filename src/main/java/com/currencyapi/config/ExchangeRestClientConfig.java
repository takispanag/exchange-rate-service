package com.currencyapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExchangeRestClientConfig {
    @Value("${exchange.rate.provider-api.key}")
    private String apiKey;

    @Value("${exchange.rate.provider-api.url}")
    private String baseUrl;

    @Bean
    public RestClient exchangeRateRestClient() {

        return RestClient.builder()
                .baseUrl(baseUrl + "?access_key=" + apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
