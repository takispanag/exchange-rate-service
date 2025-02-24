package com.exchange.validator;

import com.exchange.config.CurrencyConfig;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyValidatorTest {

    @Mock
    private CurrencyConfig currencyConfig;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    private CurrencyValidator currencyValidator;

    @BeforeEach
    void setUp() {
        when(currencyConfig.getSupported()).thenReturn(Set.of("USD", "EUR", "GBP"));
        currencyValidator = new CurrencyValidator(currencyConfig);
    }

    @Test
    void validCurrency_ReturnsTrue() {
        assertTrue(currencyValidator.isValid("USD", context));
        assertTrue(currencyValidator.isValid("EUR", context));
        assertTrue(currencyValidator.isValid("GBP", context));
    }

    @Test
    void invalidCurrency_ReturnsFalse() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertFalse(currencyValidator.isValid("invalid", context));
        assertFalse(currencyValidator.isValid("XXX", context));
        assertFalse(currencyValidator.isValid(null, context));

        verify(context, times(3)).disableDefaultConstraintViolation();
        verify(context, times(3)).buildConstraintViolationWithTemplate(anyString());
    }
}
