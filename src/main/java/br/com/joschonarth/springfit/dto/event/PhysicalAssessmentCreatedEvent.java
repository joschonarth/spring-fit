package br.com.joschonarth.springfit.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PhysicalAssessmentCreatedEvent(
        UUID assessmentId,
        UUID studentId,
        String studentName,
        BigDecimal bmi,
        String bmiClassification,
        LocalDateTime createdAt
) {}