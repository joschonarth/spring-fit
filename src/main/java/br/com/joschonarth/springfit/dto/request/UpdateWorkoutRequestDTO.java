package br.com.joschonarth.springfit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(description = "Request body for updating a workout")
public class UpdateWorkoutRequestDTO {

    @Schema(description = "Workout name", example = "Upper Body B")
    @Pattern(regexp = "^(?!\\s*$).+", message = "Name cannot be blank")
    private String name;

    @Schema(description = "Workout objective", example = "Strength")
    private String objective;

    @Schema(description = "Workout description", example = "Heavier loads")
    private String description;
}