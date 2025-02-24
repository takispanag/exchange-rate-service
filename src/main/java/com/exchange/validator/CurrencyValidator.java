package com.exchange.validator;

import com.exchange.config.CurrencyConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validator component that checks if a currency code is supported by the application.
 * Implements {@link ConstraintValidator} to provide custom validation logic for {@link ValidCurrency} annotation.
 */
@Component
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    private final Set<String> supportedCurrencies;

    /**
     * Creates a new validator with the supported currencies configuration.
     *
     * @param currencyConfig configuration containing the set of supported currency codes
     */
    public CurrencyValidator(CurrencyConfig currencyConfig) {
        this.supportedCurrencies = currencyConfig.getSupported();
    }

    /**
     * Validates if the given currency code is supported.
     *
     * @param currency the currency code to validate
     * @param context  validation context
     * @return true if the currency is not null and is in the supported currencies set
     */
    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null || !supportedCurrencies.contains(currency)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Currency '" + currency + "' is not supported.").addConstraintViolation();
            return false;
        }
        return true;
    }
}
