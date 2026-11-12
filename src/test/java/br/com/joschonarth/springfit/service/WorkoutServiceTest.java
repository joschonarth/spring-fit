package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.model.RolesEntity;
import br.com.joschonarth.springfit.database.model.UserEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.database.repository.IUserRepository;
import br.com.joschonarth.springfit.database.repository.IWorkoutRepository;
import br.com.joschonarth.springfit.dto.request.UpdateWorkoutRequestDTO;
import br.com.joschonarth.springfit.dto.request.WorkoutExercisesRequestDTO;
import br.com.joschonarth.springfit.dto.request.WorkoutRequestDTO;
import br.com.joschonarth.springfit.dto.response.ExerciseResponseDTO;
import br.com.joschonarth.springfit.dto.response.WorkoutResponseDTO;
import br.com.joschonarth.springfit.enums.DifficultyLevel;
import br.com.joschonarth.springfit.enums.MuscleGroup;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IExerciseRepository exerciseRepository;

    @Mock
    private IWorkoutRepository workoutRepository;

    @InjectMocks
    private WorkoutService workoutService;

    private UserEntity user;
    private UserEntity otherUser;
    private UserEntity admin;
    private ExerciseEntity benchPress;
    private ExerciseEntity squat;
    private UUID workoutId;
    private WorkoutEntity workout;

    @BeforeEach
    void setUp() {
        RolesEntity userRole = RolesEntity.builder().name("ROLE_STUDENT").build();
        RolesEntity adminRole = RolesEntity.builder().name("ROLE_ADMIN").build();

        user = UserEntity.builder().id(UUID.randomUUID()).name("John").roles(Set.of(userRole)).build();
        otherUser = UserEntity.builder().id(UUID.randomUUID()).name("Jane").roles(Set.of(userRole)).build();
        admin = UserEntity.builder().id(UUID.randomUUID()).name("Admin").roles(Set.of(adminRole)).build();

        benchPress = ExerciseEntity.builder().id(UUID.randomUUID()).name("Bench Press")
                .muscleGroup(MuscleGroup.CHEST).difficultyLevel(DifficultyLevel.INTERMEDIATE).build();
        squat = ExerciseEntity.builder().id(UUID.randomUUID()).name("Squat")
                .muscleGroup(MuscleGroup.QUADRICEPS).difficultyLevel(DifficultyLevel.INTERMEDIATE).build();

        workoutId = UUID.randomUUID();
        workout = WorkoutEntity.builder()
                .id(workoutId)
                .name("Upper Body A")
                .objective("Hypertrophy")
                .description("Compound movements")
                .user(user)
                .exercises(new HashSet<>(Set.of(benchPress)))
                .build();

        authenticateAs(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UserEntity user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private void stubSaveReturningArgument() {
        when(workoutRepository.save(any(WorkoutEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Nested
    @DisplayName("createWorkout")
    class CreateWorkout {

        private WorkoutRequestDTO requestFor(UUID userId) {
            return WorkoutRequestDTO.builder()
                    .userId(userId)
                    .name("Lower Body A")
                    .exerciseId(List.of(squat.getId()))
                    .build();
        }

        @Test
        @DisplayName("should create a workout for the authenticated user")
        void shouldCreateWorkoutForSelf() throws NotFoundException, BadRequestException {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(workoutRepository.findByNameAndUserId("Lower Body A", user.getId()))
                    .thenReturn(Optional.empty());
            when(exerciseRepository.findById(squat.getId())).thenReturn(Optional.of(squat));

            workoutService.createWorkout(requestFor(user.getId()));

            ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
            verify(workoutRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Lower Body A");
            assertThat(captor.getValue().getUser()).isSameAs(user);
            assertThat(captor.getValue().getExercises()).containsExactly(squat);
        }

        @Test
        @DisplayName("should throw when a user creates a workout for someone else")
        void shouldThrowWhenUserCreatesForAnotherUser() {
            assertThatThrownBy(() -> workoutService.createWorkout(requestFor(otherUser.getId())))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Users can only create workouts for themselves");

            verify(workoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when the user already has a workout with that name")
        void shouldThrowWhenNameAlreadyExists() {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(workoutRepository.findByNameAndUserId("Lower Body A", user.getId()))
                    .thenReturn(Optional.of(workout));

            assertThatThrownBy(() -> workoutService.createWorkout(requestFor(user.getId())))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Already exists a workout with this name for this user");

            verify(workoutRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getWorkoutById")
    class GetWorkoutById {

        @Test
        @DisplayName("should return the workout for its owner")
        void shouldReturnForOwner() throws NotFoundException {
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            WorkoutResponseDTO result = workoutService.getWorkoutById(workoutId);

            assertThat(result.getId()).isEqualTo(workoutId);
            assertThat(result.getUserId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("should return another user's workout for ADMIN")
        void shouldReturnForAdmin() throws NotFoundException {
            authenticateAs(admin);
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            assertThat(workoutService.getWorkoutById(workoutId).getId()).isEqualTo(workoutId);
        }

        @Test
        @DisplayName("should throw NotFoundException when a user accesses another user's workout")
        void shouldThrowForAnotherUser() {
            authenticateAs(otherUser);
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            assertThatThrownBy(() -> workoutService.getWorkoutById(workoutId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Workout not found");
        }
    }

    @Nested
    @DisplayName("getWorkoutsByUserId")
    class GetWorkoutsByUserId {

        @Test
        @DisplayName("should return the workouts of the given user")
        void shouldReturnWorkouts() throws NotFoundException {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(workoutRepository.findAllByUserId(user.getId())).thenReturn(List.of(workout));

            assertThat(workoutService.getWorkoutsByUserId(user.getId())).hasSize(1);
        }

        @Test
        @DisplayName("should throw NotFoundException when the user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workoutService.getWorkoutsByUserId(user.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("updateWorkout")
    class UpdateWorkout {

        @Test
        @DisplayName("should update only the provided fields")
        void shouldUpdateOnlyProvidedFields() throws NotFoundException, BadRequestException {
            UpdateWorkoutRequestDTO dto = new UpdateWorkoutRequestDTO();
            dto.setObjective("Strength");

            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
            stubSaveReturningArgument();

            WorkoutResponseDTO result = workoutService.updateWorkout(workoutId, dto);

            assertThat(result.getObjective()).isEqualTo("Strength");
            assertThat(result.getName()).isEqualTo("Upper Body A");
        }

        @Test
        @DisplayName("should throw when the new name is already used by the user")
        void shouldThrowWhenNewNameAlreadyExists() {
            UpdateWorkoutRequestDTO dto = new UpdateWorkoutRequestDTO();
            dto.setName("Lower Body A");

            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
            when(workoutRepository.findByNameAndUserId("Lower Body A", user.getId()))
                    .thenReturn(Optional.of(WorkoutEntity.builder().name("Lower Body A").build()));

            assertThatThrownBy(() -> workoutService.updateWorkout(workoutId, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Already exists a workout with this name for this user");

            verify(workoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw NotFoundException when a user updates another user's workout")
        void shouldThrowForAnotherUser() {
            authenticateAs(otherUser);
            UpdateWorkoutRequestDTO dto = new UpdateWorkoutRequestDTO();
            dto.setName("Hacked");

            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            assertThatThrownBy(() -> workoutService.updateWorkout(workoutId, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Workout not found");

            verify(workoutRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteWorkout")
    class DeleteWorkout {

        @Test
        @DisplayName("should delete the workout for its owner")
        void shouldDeleteForOwner() throws NotFoundException {
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            workoutService.deleteWorkout(workoutId);

            verify(workoutRepository).delete(workout);
        }

        @Test
        @DisplayName("should throw NotFoundException when a user deletes another user's workout")
        void shouldThrowForAnotherUser() {
            authenticateAs(otherUser);
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            assertThatThrownBy(() -> workoutService.deleteWorkout(workoutId))
                    .isInstanceOf(NotFoundException.class);

            verify(workoutRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("addExercises")
    class AddExercises {

        private WorkoutExercisesRequestDTO requestWith(UUID... ids) {
            WorkoutExercisesRequestDTO dto = new WorkoutExercisesRequestDTO();
            dto.setExerciseIds(List.of(ids));
            return dto;
        }

        @Test
        @DisplayName("should add exercises and ignore the ones already in the workout")
        void shouldAddExercisesWithoutDuplicates() throws NotFoundException {
            WorkoutExercisesRequestDTO dto = requestWith(benchPress.getId(), squat.getId());

            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
            when(exerciseRepository.findAllById(dto.getExerciseIds())).thenReturn(List.of(benchPress, squat));
            stubSaveReturningArgument();

            WorkoutResponseDTO result = workoutService.addExercises(workoutId, dto);

            assertThat(result.getExercises())
                    .extracting(ExerciseResponseDTO::getName)
                    .containsExactlyInAnyOrder("Bench Press", "Squat");
        }

        @Test
        @DisplayName("should throw NotFoundException when an exercise does not exist")
        void shouldThrowWhenExerciseNotFound() {
            UUID missingId = UUID.randomUUID();
            WorkoutExercisesRequestDTO dto = requestWith(squat.getId(), missingId);

            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
            when(exerciseRepository.findAllById(dto.getExerciseIds())).thenReturn(List.of(squat));

            assertThatThrownBy(() -> workoutService.addExercises(workoutId, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(String.format("Exercise %s not found", missingId));

            verify(workoutRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeExercise")
    class RemoveExercise {

        @Test
        @DisplayName("should remove the exercise from the workout")
        void shouldRemoveExercise() throws NotFoundException, BadRequestException {
            workout.getExercises().add(squat);

            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
            stubSaveReturningArgument();

            WorkoutResponseDTO result = workoutService.removeExercise(workoutId, squat.getId());

            assertThat(result.getExercises())
                    .extracting(ExerciseResponseDTO::getName)
                    .containsExactly("Bench Press");
        }

        @Test
        @DisplayName("should throw and keep the exercise when it is the last one")
        void shouldThrowWhenRemovingLastExercise() {
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            assertThatThrownBy(() -> workoutService.removeExercise(workoutId, benchPress.getId()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("A workout must have at least one exercise");

            assertThat(workout.getExercises()).containsExactly(benchPress);
            verify(workoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw NotFoundException when the exercise is not in the workout")
        void shouldThrowWhenExerciseNotInWorkout() {
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

            assertThatThrownBy(() -> workoutService.removeExercise(workoutId, squat.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Exercise not found in this workout");
        }
    }
}