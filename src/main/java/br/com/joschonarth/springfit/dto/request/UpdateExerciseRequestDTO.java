package br.com.joschonarth.springfit.dto.request;

import br.com.joschonarth.springfit.enums.DifficultyLevel;
import br.com.joschonarth.springfit.enums.MuscleGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(description = "Request body for creating an exercise")
public class UpdateExerciseRequestDTO {

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
}
