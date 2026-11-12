package br.com.joschonarth.springfit.controller;

import br.com.joschonarth.springfit.dto.response.PhysicalAssessmentResponseDTO;
import br.com.joschonarth.springfit.dto.response.UserResponseDTO;
import br.com.joschonarth.springfit.dto.request.UpdateUserRequestDTO;
import br.com.joschonarth.springfit.dto.response.WorkoutResponseDTO;
import br.com.joschonarth.springfit.exception.NotFoundException;
import br.com.joschonarth.springfit.service.UserService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User", description = "Endpoints for user management")
@RestController
@RequestMapping("v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WorkoutService workoutService;

    @Operation(summary = "Get user physical assessment by ID", description = "Users can only view their own assessments. ADMIN can view all.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot access another user's assessment"),
            @ApiResponse(responseCode = "404", description = "User or assessment not found")
    })
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("{userId}/assessment/{assessmentId}")
    public PhysicalAssessmentResponseDTO getPhysicalAssessment(
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId,
            @Parameter(description = "Assessment ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assessmentId) throws NotFoundException {
        return userService.getUserAssessment(userId, assessmentId);
    }

    @Operation(summary = "Get all physical assessments from a user", description = "Users can only view their own assessments. ADMIN can view all.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot access another user's assessments"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("{userId}/assessments")
    public List<PhysicalAssessmentResponseDTO> getPhysicalAssessments(
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId) throws NotFoundException {
        return userService.getUserAssessments(userId);
    }

    @Operation(summary = "Remove a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot remove another user's account"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId) throws NotFoundException {
        userService.deleteUser(userId);
    }

    @Operation(summary = "List all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot access another user's profile"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getUserById(
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId) throws NotFoundException {
        return userService.getUserById(userId);
    }

    @Operation(summary = "Update user data", description = "Users can only update their own data. ADMIN can update any user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot update another user's data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("{userId}")
    public UserResponseDTO updateUser(
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequestDTO dto) throws NotFoundException {
        return userService.updateUser(userId, dto);
    }

    @Operation(summary = "List all workouts from a user",
            description = "Users can only view their own workouts. ADMIN can view any user's workouts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workouts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot access another user's workouts"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("{userId}/workouts")
    @ResponseStatus(HttpStatus.OK)
    public List<WorkoutResponseDTO> getUserWorkouts(
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId) throws NotFoundException {
        return workoutService.getWorkoutsByUserId(userId);
    }
}
