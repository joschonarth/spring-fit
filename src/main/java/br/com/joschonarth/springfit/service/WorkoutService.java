package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.model.StudentEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.database.repository.IStudentRepository;
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
        return toResponseDTO(getAccessibleWorkout(workoutId));
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

    @Transactional(readOnly = true)
    public List<WorkoutResponseDTO> getWorkoutsByStudentId(UUID studentId) throws NotFoundException {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        return workoutRepository.findAllByStudentId(studentId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public WorkoutResponseDTO updateWorkout(UUID workoutId, UpdateWorkoutRequestDTO dto)
            throws NotFoundException, BadRequestException {
        WorkoutEntity workout = getAccessibleWorkout(workoutId);

        if (dto.getName() != null && !dto.getName().equals(workout.getName())) {
            boolean exists = workoutRepository
                    .findByNameAndStudentId(dto.getName(), workout.getStudent().getId())
                    .isPresent();
            if (exists) {
                throw new BadRequestException("Already exists a workout with this name for this student");
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

    private StudentEntity getAuthenticatedStudent() {
        return (StudentEntity) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication(),
                "Authentication must not be null"
        ).getPrincipal();
    }

    private boolean isAdmin(StudentEntity student) {
        return student.getAuthorities().stream()
                .anyMatch(r -> RoleTypeEnum.ROLE_ADMIN.name().equals(r.getAuthority()));
    }

    private WorkoutEntity getAccessibleWorkout(UUID workoutId) throws NotFoundException {
        StudentEntity auth = getAuthenticatedStudent();

        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new NotFoundException("Workout not found"));

        if (!isAdmin(auth) && !workout.getStudent().getId().equals(auth.getId())) {
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
                .studentId(workout.getStudent().getId())
                .createdAt(workout.getCreatedAt())
                .updatedAt(workout.getUpdatedAt())
                .exercises(exercises)
                .build();
    }
}
