package br.com.joschonarth.springfit.controller;

import br.com.joschonarth.springfit.dto.request.WorkoutRequestDTO;
import br.com.joschonarth.springfit.dto.response.WorkoutResponseDTO;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import br.com.joschonarth.springfit.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Workout", description = "Endpoints for workout management")
@RestController
@RequestMapping("v1/workout")
@RequiredArgsConstructor
@Validated
public class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "Create a new workout")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or workout already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Student or exercise not found")
    })
    @PreAuthorize("#workoutRequestDTO.studentId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createWorkout(@Valid @RequestBody WorkoutRequestDTO workoutRequestDTO) throws NotFoundException, BadRequestException {
        workoutService.createWorkout(workoutRequestDTO);
    }

    @Operation(summary = "Get workout by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Workout not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("{workoutId}")
    @ResponseStatus(HttpStatus.OK)
    public WorkoutResponseDTO getWorkoutById(
            @Parameter(description = "Workout ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID workoutId) throws NotFoundException {
        return workoutService.getWorkoutById(workoutId);
    }

    @Operation(summary = "List all workouts from authenticated student")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workouts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WorkoutResponseDTO> getAllWorkouts() {
        return workoutService.getAllWorkouts();
    }
}
