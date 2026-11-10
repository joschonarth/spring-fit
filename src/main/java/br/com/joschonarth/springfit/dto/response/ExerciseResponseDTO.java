package br.com.joschonarth.springfit.dto.response;

import br.com.joschonarth.springfit.enums.DifficultyLevel;
import br.com.joschonarth.springfit.enums.MuscleGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Exercise response data")
public class ExerciseResponseDTO {

    @Schema(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Exercise name", example = "Bench Press")
    private String name;

    @Schema(description = "Muscle group targeted", example = "CHEST")
    private MuscleGroup muscleGroup;

    @Schema(description = "Equipment needed", example = "Barbell")
    private String equipment;

    @Schema(description = "Exercise description", example = "Lie on a flat bench and push the barbell upward")
    private String description;

    @Schema(description = "Difficulty level", example = "INTERMEDIATE")
    private DifficultyLevel difficultyLevel;

    @Schema(description = "Creation date")
    private LocalDateTime createdAt;
}