package br.com.joschonarth.springfit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class MaxBytesValidator implements ConstraintValidator<MaxBytes, CharSequence> {

    private int max;

    @Override
    public void initialize(MaxBytes annotation) {
        this.max = annotation.value();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.toString().getBytes(StandardCharsets.UTF_8).length <= max;
    }
}