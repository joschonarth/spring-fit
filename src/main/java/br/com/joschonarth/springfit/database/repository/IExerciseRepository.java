package br.com.joschonarth.springfit.database.repository;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IExerciseRepository extends JpaRepository<ExerciseEntity, UUID> {

    List<ExerciseEntity> findByMuscleGroupIgnoreCase(String muscleGroup);
}
