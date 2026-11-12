package br.com.joschonarth.springfit.config;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.model.RolesEntity;
import br.com.joschonarth.springfit.database.model.UserEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.database.repository.IRolesRepository;
import br.com.joschonarth.springfit.database.repository.IUserRepository;
import br.com.joschonarth.springfit.enums.DifficultyLevel;
import br.com.joschonarth.springfit.enums.MuscleGroup;
import br.com.joschonarth.springfit.enums.RoleTypeEnum;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final IRolesRepository rolesRepository;
    private final IExerciseRepository exerciseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {

        if (userRepository.count() > 0) return;

        RolesEntity adminRole = rolesRepository.findByName(RoleTypeEnum.ROLE_ADMIN.name())
                .orElseGet(() -> rolesRepository.save(RolesEntity.builder()
                        .name(RoleTypeEnum.ROLE_ADMIN.name())
                        .build()));

        RolesEntity userRole = rolesRepository.findByName(RoleTypeEnum.ROLE_STUDENT.name())
                .orElseGet(() -> rolesRepository.save(RolesEntity.builder()
                        .name(RoleTypeEnum.ROLE_STUDENT.name())
                        .build()));

        userRepository.saveAll(List.of(
                UserEntity.builder()
                        .name("Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .phone("(11) 99999-0001")
                        .roles(Set.of(adminRole))
                        .build(),
                UserEntity.builder()
                        .name("John Doe")
                        .email("johndoe@example.com")
                        .password(passwordEncoder.encode("john123"))
                        .birthDate(LocalDate.of(2000, 6, 15))
                        .phone("(11) 99999-0002")
                        .roles(Set.of(userRole))
                        .build()
        ));

        exerciseRepository.saveAll(List.of(
                ExerciseEntity.builder()
                        .name("Bench Press")
                        .muscleGroup(MuscleGroup.CHEST)
                        .equipment("Barbell")
                        .description("Compound chest exercise focused on strength and hypertrophy.")
                        .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                        .build(),

                ExerciseEntity.builder()
                        .name("Pull Up")
                        .muscleGroup(MuscleGroup.BACK)
                        .equipment("Bar")
                        .description("Bodyweight exercise that strengthens the back and biceps.")
                        .difficultyLevel(DifficultyLevel.ADVANCED)
                        .build(),

                ExerciseEntity.builder()
                        .name("Squat")
                        .muscleGroup(MuscleGroup.QUADRICEPS)
                        .equipment("Barbell")
                        .description("Fundamental lower body exercise targeting quadriceps.")
                        .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                        .build(),

                ExerciseEntity.builder()
                        .name("Shoulder Press")
                        .muscleGroup(MuscleGroup.SHOULDERS)
                        .equipment("Dumbbell")
                        .description("Upper body exercise focused on shoulder development.")
                        .difficultyLevel(DifficultyLevel.BEGINNER)
                        .build(),

                ExerciseEntity.builder()
                        .name("Bicep Curl")
                        .muscleGroup(MuscleGroup.FOREARMS)
                        .equipment("Dumbbell")
                        .description("Isolation exercise designed to strengthen the biceps.")
                        .difficultyLevel(DifficultyLevel.BEGINNER)
                        .build()
        ));
    }
}