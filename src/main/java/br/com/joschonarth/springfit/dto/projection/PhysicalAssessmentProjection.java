package br.com.joschonarth.springfit.dto.projection;

import br.com.joschonarth.springfit.enums.BmiClassification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface PhysicalAssessmentProjection {

    UUID getUserId();
    String getUserName();
    UUID getAssessmentId();
    BigDecimal getWeight();
    BigDecimal getHeight();
    BigDecimal getBodyFatPercentage();
    BigDecimal getBmi();
    BmiClassification getBmiClassification();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
