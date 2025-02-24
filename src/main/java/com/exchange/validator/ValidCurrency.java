package com.exchange.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for currency codes.
 * Used to validate that a currency code is supported by the application.
 *
 * <p>Usage example:
 * {@code @ValidCurrency private String currencyCode;}
 * {@code public void convert(@ValidCurrency String sourceCurrency) {...}}
 */
@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    /**
     * @return the error message template
     */
    String message() default "Invalid currency code";

    /**
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};
}
