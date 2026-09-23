package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.dto.request.ExerciseRequestDTO;
import br.com.joschonarth.springfit.dto.request.UpdateExerciseRequestDTO;
import br.com.joschonarth.springfit.dto.response.ExerciseResponseDTO;
import br.com.joschonarth.springfit.enums.DifficultyLevel;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private IExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private ExerciseEntity exercise;
    private UUID exerciseId;

    @BeforeEach
    void setUp() {
        exerciseId = UUID.randomUUID();
        exercise = ExerciseEntity.builder()
                .id(exerciseId)
                .name("Bench Press")
                .muscleGroup("CHEST")
                .equipment("Barbell")
                .description("Compound chest exercise")
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .workouts(new HashSet<>())
                .build();
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all exercises mapped to response DTO")
        void shouldReturnAllExercises() {
            when(exerciseRepository.findAll()).thenReturn(List.of(exercise));

            List<ExerciseResponseDTO> result = exerciseService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(exerciseId);
            assertThat(result.get(0).getName()).isEqualTo("Bench Press");
            verify(exerciseRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when there are no exercises")
        void shouldReturnEmptyList() {
            when(exerciseRepository.findAll()).thenReturn(List.of());

            List<ExerciseResponseDTO> result = exerciseService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should save exercise with provided difficulty level")
        void shouldSaveWithProvidedDifficultyLevel() {
            ExerciseRequestDTO dto = ExerciseRequestDTO.builder()
                    .name("Squat")
                    .muscleGroup("LEGS")
                    .equipment("Barbell")
                    .description("Leg exercise")
                    .difficultyLevel(DifficultyLevel.ADVANCED)
                    .build();

            exerciseService.save(dto);

            ArgumentCaptor<ExerciseEntity> captor = ArgumentCaptor.forClass(ExerciseEntity.class);
            verify(exerciseRepository).save(captor.capture());
            assertThat(captor.getValue().getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
            assertThat(captor.getValue().getName()).isEqualTo("Squat");
        }

        @Test
        @DisplayName("should default difficulty level to BEGINNER when not provided")
        void shouldDefaultDifficultyLevelToBeginner() {
            ExerciseRequestDTO dto = ExerciseRequestDTO.builder()
                    .name("Plank")
                    .muscleGroup("CORE")
                    .build();

            exerciseService.save(dto);

            ArgumentCaptor<ExerciseEntity> captor = ArgumentCaptor.forClass(ExerciseEntity.class);
            verify(exerciseRepository).save(captor.capture());
            assertThat(captor.getValue().getDifficultyLevel()).isEqualTo(DifficultyLevel.BEGINNER);
        }
    }

    @Nested
    @DisplayName("getExerciseById")
    class GetExerciseById {

        @Test
        @DisplayName("should return exercise when found")
        void shouldReturnExerciseWhenFound() throws NotFoundException {
            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

            ExerciseResponseDTO result = exerciseService.getExerciseById(exerciseId);

            assertThat(result.getId()).isEqualTo(exerciseId);
            assertThat(result.getName()).isEqualTo("Bench Press");
        }

        @Test
        @DisplayName("should throw NotFoundException when exercise does not exist")
        void shouldThrowWhenNotFound() {
            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> exerciseService.getExerciseById(exerciseId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Exercise not found");
        }
    }

    @Nested
    @DisplayName("getExerciseByMuscleGroup")
    class GetExerciseByMuscleGroup {

        @Test
        @DisplayName("should return exercises filtered by muscle group")
        void shouldReturnExercisesByMuscleGroup() {
            when(exerciseRepository.findByMuscleGroupIgnoreCase("chest")).thenReturn(List.of(exercise));

            List<ExerciseResponseDTO> result = exerciseService.getExerciseByMuscleGroup("chest");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMuscleGroup()).isEqualTo("CHEST");
        }

        @Test
        @DisplayName("should return empty list when no exercise matches muscle group")
        void shouldReturnEmptyListWhenNoMatch() {
            when(exerciseRepository.findByMuscleGroupIgnoreCase(anyString())).thenReturn(List.of());

            List<ExerciseResponseDTO> result = exerciseService.getExerciseByMuscleGroup("UNKNOWN");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateExercise")
    class UpdateExercise {

        @Test
        @DisplayName("should update only provided fields")
        void shouldUpdateOnlyProvidedFields() throws NotFoundException {
            UpdateExerciseRequestDTO dto = UpdateExerciseRequestDTO.builder()
                    .name("Incline Bench Press")
                    .difficultyLevel(DifficultyLevel.ADVANCED)
                    .build();

            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
            when(exerciseRepository.save(any(ExerciseEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ExerciseResponseDTO result = exerciseService.updateExercise(exerciseId, dto);

            assertThat(result.getName()).isEqualTo("Incline Bench Press");
            assertThat(result.getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
            // fields not provided must remain unchanged
            assertThat(result.getMuscleGroup()).isEqualTo("CHEST");
            assertThat(result.getEquipment()).isEqualTo("Barbell");
        }

        @Test
        @DisplayName("should throw NotFoundException when exercise does not exist")
        void shouldThrowWhenExerciseNotFound() {
            UpdateExerciseRequestDTO dto = UpdateExerciseRequestDTO.builder().name("New Name").build();
            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> exerciseService.updateExercise(exerciseId, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Exercise not found");

            verify(exerciseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteExercise")
    class DeleteExercise {

        @Test
        @DisplayName("should delete exercise when it has no associated workouts")
        void shouldDeleteExerciseWithoutWorkouts() throws NotFoundException, BadRequestException {
            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

            exerciseService.deleteExercise(exerciseId);

            verify(exerciseRepository).deleteById(exerciseId);
        }

        @Test
        @DisplayName("should throw BadRequestException when exercise is associated with workouts")
        void shouldThrowWhenExerciseHasWorkouts() {
            Set<WorkoutEntity> workouts = new HashSet<>();
            workouts.add(WorkoutEntity.builder().id(UUID.randomUUID()).build());
            exercise.setWorkouts(workouts);

            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

            assertThatThrownBy(() -> exerciseService.deleteExercise(exerciseId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot delete exercise because it is associated with one or more workouts");

            verify(exerciseRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw NotFoundException when exercise does not exist")
        void shouldThrowWhenExerciseNotFound() {
            when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> exerciseService.deleteExercise(exerciseId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Exercise not found");
        }
    }
}