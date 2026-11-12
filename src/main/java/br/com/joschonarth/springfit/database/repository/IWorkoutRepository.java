package br.com.joschonarth.springfit.database.repository;

import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IWorkoutRepository extends JpaRepository<WorkoutEntity, UUID> {

    Optional<WorkoutEntity> findByNameAndUserId(String name, UUID userId);

    Optional<WorkoutEntity> findByIdAndUserId(UUID workoutId, UUID userId);

    List<WorkoutEntity> findAllByUserId(UUID userId);
}