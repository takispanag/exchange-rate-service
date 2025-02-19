package com.exchange.validator;


import com.exchange.dto.CurrencyDto;
import com.exchange.service.CurrencyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private final CurrencyService currencyService;
    private Set<String> validCurrencyCodes;

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        validCurrencyCodes = currencyService.getAvailableCurrencies()
                .stream()
                .map(CurrencyDto::getCode)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return validCurrencyCodes.contains(value.toUpperCase());
    }
}

