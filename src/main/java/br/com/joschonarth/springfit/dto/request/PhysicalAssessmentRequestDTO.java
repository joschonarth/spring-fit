package br.com.joschonarth.springfit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for creating a physical assessment")
public class PhysicalAssessmentRequestDTO {

    @Schema(description = "Student ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull
    private UUID studentId;

    @Schema(description = "Student weight in kg", example = "85.5")
    @NotNull
    private BigDecimal weight;

    @Schema(description = "Student height in meters", example = "1.85")
    @NotNull
    private BigDecimal height;

    @Schema(description = "Body fat percentage", example = "18.5")
    @NotNull
    private BigDecimal bodyFatPercentage;
}
