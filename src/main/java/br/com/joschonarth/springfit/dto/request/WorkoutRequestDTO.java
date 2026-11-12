package br.com.joschonarth.springfit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(description = "Request body for creating a workout")
public class WorkoutRequestDTO {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull
    private UUID userId;

    @Schema(description = "Workout name", example = "Upper Body A")
    @NotBlank
    private String name;

    @Schema(description = "Workout objective", example = "Muscle hypertrophy")
    private String objective;

    @Schema(description = "Workout description", example = "Focus on compound movements")
    private String description;

    @Schema(description = "List of exercise IDs", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    @NotEmpty
    private List<UUID> exerciseId;
}
