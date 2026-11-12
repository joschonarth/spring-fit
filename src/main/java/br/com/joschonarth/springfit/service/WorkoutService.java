package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.model.UserEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.database.repository.IUserRepository;
import br.com.joschonarth.springfit.database.repository.IWorkoutRepository;
import br.com.joschonarth.springfit.dto.request.UpdateWorkoutRequestDTO;
import br.com.joschonarth.springfit.dto.request.WorkoutExercisesRequestDTO;
import br.com.joschonarth.springfit.dto.response.ExerciseResponseDTO;
import br.com.joschonarth.springfit.dto.request.WorkoutRequestDTO;
import br.com.joschonarth.springfit.dto.response.WorkoutResponseDTO;
import br.com.joschonarth.springfit.enums.RoleTypeEnum;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final IUserRepository userRepository;
    private final IExerciseRepository exerciseRepository;
    private final IWorkoutRepository workoutRepository;

    public void createWorkout(WorkoutRequestDTO workoutRequestDTO) throws NotFoundException, BadRequestException {
        UserEntity authenticatedUser = Objects.requireNonNull(
                (UserEntity) Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication(),
                        "Authentication must not be null"
                ).getPrincipal(),
                "Authenticated user must not be null"
        );

        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN"));

        UUID targetUserId = workoutRequestDTO.getUserId();

        if (!isAdmin && !authenticatedUser.getId().equals(targetUserId)) {
            throw new BadRequestException("Users can only create workouts for themselves");
        }

        Set<ExerciseEntity> exercises = new HashSet<>();

        UserEntity user = userRepository.findById(workoutRequestDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        WorkoutEntity workout = workoutRepository.findByNameAndUserId(workoutRequestDTO.getName(), workoutRequestDTO.getUserId())
                .orElse(null);

        if (workout != null) {
            throw new BadRequestException("Already exists a workout with this name for this user");
        }

        for (UUID exerciseId : workoutRequestDTO.getExerciseId()) {
            ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new NotFoundException(String.format("Exercise %s not found", exerciseId)));

            exercises.add(exercise);
        }

        workout = WorkoutEntity.builder()
                .name(workoutRequestDTO.getName())
                .objective(workoutRequestDTO.getObjective())
                .description(workoutRequestDTO.getDescription())
                .user(user)
                .exercises(exercises)
                .build();

        workoutRepository.save(workout);
    }

    public WorkoutResponseDTO getWorkoutById(UUID workoutId) throws NotFoundException {
        return toResponseDTO(getAccessibleWorkout(workoutId));
    }

    public List<WorkoutResponseDTO> getAllWorkouts() {
        UserEntity authenticatedUser = Objects.requireNonNull(
                (UserEntity) Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication(),
                        "Authentication must not be null"
                ).getPrincipal(),
                "Authenticated user must not be null"
        );
        return workoutRepository.findAllByUserId(authenticatedUser.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponseDTO> getWorkoutsByUserId(UUID userId) throws NotFoundException {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return workoutRepository.findAllByUserId(userId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public WorkoutResponseDTO updateWorkout(UUID workoutId, UpdateWorkoutRequestDTO dto)
            throws NotFoundException, BadRequestException {
        WorkoutEntity workout = getAccessibleWorkout(workoutId);

        if (dto.getName() != null && !dto.getName().equals(workout.getName())) {
            boolean exists = workoutRepository
                    .findByNameAndUserId(dto.getName(), workout.getUser().getId())
                    .isPresent();
            if (exists) {
                throw new BadRequestException("Already exists a workout with this name for this user");
            }
            workout.setName(dto.getName());
        }
        if (dto.getObjective() != null) workout.setObjective(dto.getObjective());
        if (dto.getDescription() != null) workout.setDescription(dto.getDescription());

        return toResponseDTO(workoutRepository.save(workout));
    }

    @Transactional
    public void deleteWorkout(UUID workoutId) throws NotFoundException {
        workoutRepository.delete(getAccessibleWorkout(workoutId));
    }

    @Transactional
    public WorkoutResponseDTO addExercises(UUID workoutId, WorkoutExercisesRequestDTO dto)
            throws NotFoundException {
        WorkoutEntity workout = getAccessibleWorkout(workoutId);

        List<ExerciseEntity> found = exerciseRepository.findAllById(dto.getExerciseIds());
        Set<UUID> foundIds = found.stream().map(ExerciseEntity::getId).collect(Collectors.toSet());

        for (UUID id : dto.getExerciseIds()) {
            if (!foundIds.contains(id)) {
                throw new NotFoundException(String.format("Exercise %s not found", id));
            }
        }

        workout.getExercises().addAll(found);
        return toResponseDTO(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutResponseDTO removeExercise(UUID workoutId, UUID exerciseId)
            throws NotFoundException, BadRequestException {
        WorkoutEntity workout = getAccessibleWorkout(workoutId);

        boolean inWorkout = workout.getExercises().stream()
                .anyMatch(e -> e.getId().equals(exerciseId));
        if (!inWorkout) {
            throw new NotFoundException("Exercise not found in this workout");
        }

        if (workout.getExercises().size() == 1) {
            throw new BadRequestException("A workout must have at least one exercise");
        }

        workout.getExercises().removeIf(e -> e.getId().equals(exerciseId));
        return toResponseDTO(workoutRepository.save(workout));
    }

    private UserEntity getAuthenticatedUser() {
        return (UserEntity) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication(),
                "Authentication must not be null"
        ).getPrincipal();
    }

    private boolean isAdmin(UserEntity user) {
        return user.getAuthorities().stream()
                .anyMatch(r -> RoleTypeEnum.ROLE_ADMIN.name().equals(r.getAuthority()));
    }

    private WorkoutEntity getAccessibleWorkout(UUID workoutId) throws NotFoundException {
        UserEntity auth = getAuthenticatedUser();

        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new NotFoundException("Workout not found"));

        if (!isAdmin(auth) && !workout.getUser().getId().equals(auth.getId())) {
            throw new NotFoundException("Workout not found");
        }
        return workout;
    }

    private WorkoutResponseDTO toResponseDTO(WorkoutEntity workout) {
        List<ExerciseResponseDTO> exercises = workout.getExercises().stream()
                .map(exercise -> ExerciseResponseDTO.builder()
                        .id(exercise.getId())
                        .name(exercise.getName())
                        .muscleGroup(exercise.getMuscleGroup())
                        .equipment(exercise.getEquipment())
                        .description(exercise.getDescription())
                        .difficultyLevel(exercise.getDifficultyLevel())
                        .createdAt(exercise.getCreatedAt())
                        .build())
                .toList();

        return WorkoutResponseDTO.builder()
                .id(workout.getId())
                .name(workout.getName())
                .objective(workout.getObjective())
                .description(workout.getDescription())
                .userId(workout.getUser().getId())
                .createdAt(workout.getCreatedAt())
                .updatedAt(workout.getUpdatedAt())
                .exercises(exercises)
                .build();
    }
}
