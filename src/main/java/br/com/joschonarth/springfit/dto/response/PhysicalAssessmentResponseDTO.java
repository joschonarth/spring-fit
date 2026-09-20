package br.com.joschonarth.springfit.dto.response;

import br.com.joschonarth.springfit.enums.BmiClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Physical assessment response data")
public class PhysicalAssessmentResponseDTO {

    @Schema(description = "Assessment ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Student ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID studentId;

    @Schema(description = "Student weight in kg", example = "85.5")
    private BigDecimal weight;

    @Schema(description = "Student height in meters", example = "1.85")
    private BigDecimal height;

    @Schema(description = "Body fat percentage", example = "18.5")
    private BigDecimal bodyFatPercentage;

    @Schema(description = "BMI (Body Mass Index)", example = "20.78")
    private BigDecimal bmi;

    @Schema(description = "BMI classification", example = "NORMAL")
    private BmiClassification bmiClassification;

    @Schema(description = "Creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last update date")
    private LocalDateTime updatedAt;
}