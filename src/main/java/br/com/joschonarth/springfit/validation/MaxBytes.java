package br.com.joschonarth.springfit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {

    String message() default "Value exceeds the maximum number of bytes";

    int value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}