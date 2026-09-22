package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.model.StudentEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.database.repository.IStudentRepository;
import br.com.joschonarth.springfit.database.repository.IWorkoutRepository;
import br.com.joschonarth.springfit.dto.response.ExerciseResponseDTO;
import br.com.joschonarth.springfit.dto.request.WorkoutRequestDTO;
import br.com.joschonarth.springfit.dto.response.WorkoutResponseDTO;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final IStudentRepository studentRepository;
    private final IExerciseRepository exerciseRepository;
    private final IWorkoutRepository workoutRepository;

    public void createWorkout(WorkoutRequestDTO workoutRequestDTO) throws NotFoundException, BadRequestException {
        StudentEntity authenticatedStudent = Objects.requireNonNull(
                (StudentEntity) Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication(),
                        "Authentication must not be null"
                ).getPrincipal(),
                "Authenticated student must not be null"
        );

        boolean isAdmin = authenticatedStudent.getAuthorities().stream()
                .anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN"));

        UUID targetStudentId = workoutRequestDTO.getStudentId();

        if (!isAdmin && !authenticatedStudent.getId().equals(targetStudentId)) {
            throw new BadRequestException("Students can only create workouts for themselves");
        }

        Set<ExerciseEntity> exercises = new HashSet<>();

        StudentEntity student = studentRepository.findById(workoutRequestDTO.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));

        WorkoutEntity workout = workoutRepository.findByNameAndStudentId(workoutRequestDTO.getName(), workoutRequestDTO.getStudentId())
                .orElse(null);

        if (workout != null) {
            throw new BadRequestException("Already exists a workout with this name for this student");
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
                .student(student)
                .exercises(exercises)
                .build();

        workoutRepository.save(workout);
    }

    public WorkoutResponseDTO getWorkoutById(UUID workoutId) throws NotFoundException {
        Authentication authentication = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication(),
                "Authentication must not be null"
        );

        StudentEntity authenticatedStudent = Objects.requireNonNull(
                (StudentEntity) authentication.getPrincipal(),
                "Authenticated student must not be null"
        );

        WorkoutEntity workout = workoutRepository.findByIdAndStudentId(workoutId, authenticatedStudent.getId())
                .orElseThrow(() -> new NotFoundException("Workout not found"));

        return toResponseDTO(workout);
    }

    public List<WorkoutResponseDTO> getAllWorkouts() {
        StudentEntity authenticatedStudent = Objects.requireNonNull(
                (StudentEntity) Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication(),
                        "Authentication must not be null"
                ).getPrincipal(),
                "Authenticated student must not be null"
        );
        return workoutRepository.findAllByStudentId(authenticatedStudent.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
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
                .studentId(workout.getStudent().getId())
                .createdAt(workout.getCreatedAt())
                .updatedAt(workout.getUpdatedAt())
                .exercises(exercises)
                .build();
    }
}
