package br.com.joschonarth.springfit.controller;

import br.com.joschonarth.springfit.dto.request.ExerciseRequestDTO;
import br.com.joschonarth.springfit.dto.response.ExerciseResponseDTO;
import br.com.joschonarth.springfit.dto.request.UpdateExerciseRequestDTO;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import br.com.joschonarth.springfit.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Exercise", description = "Endpoints for exercise management")
@RestController
@RequestMapping("v1/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(summary = "List all exercises")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ExerciseResponseDTO> findAll() {
        return exerciseService.findAll();
    }

    @Operation(summary = "Create a new exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercise created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can create exercises")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveExercise(@Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO) {
        exerciseService.save(exerciseRequestDTO);
    }

    @Operation(summary = "List exercises by muscle group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/group/{muscleGroup}")
    @ResponseStatus(HttpStatus.OK)
    public List<ExerciseResponseDTO> getExerciseByMuscleGroup(
            @Parameter(description = "Muscle group name", example = "CHEST")
            @PathVariable String muscleGroup) {
        return exerciseService.getExerciseByMuscleGroup(muscleGroup);
    }

    @Operation(summary = "Get exercise by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @GetMapping("{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseResponseDTO getExerciseById(
            @Parameter(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID exerciseId) throws NotFoundException {
        return exerciseService.getExerciseById(exerciseId);
    }

    @Operation(summary = "Update an exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can update exercises"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{exerciseId}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseResponseDTO updateExercise(
            @Parameter(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID exerciseId,
            @Valid @RequestBody UpdateExerciseRequestDTO dto) throws NotFoundException {
        return exerciseService.updateExercise(exerciseId, dto);
    }

    @Operation(summary = "Remove an exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exercise removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can remove exercises"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(
            @Parameter(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID exerciseId) throws NotFoundException, BadRequestException {
        exerciseService.deleteExercise(exerciseId);
    }
}
