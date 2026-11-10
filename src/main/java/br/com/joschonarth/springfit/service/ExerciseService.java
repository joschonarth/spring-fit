package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.ExerciseEntity;
import br.com.joschonarth.springfit.database.repository.IExerciseRepository;
import br.com.joschonarth.springfit.dto.request.ExerciseRequestDTO;
import br.com.joschonarth.springfit.dto.request.UpdateExerciseRequestDTO;
import br.com.joschonarth.springfit.dto.response.ExerciseResponseDTO;
import br.com.joschonarth.springfit.enums.DifficultyLevel;
import br.com.joschonarth.springfit.enums.MuscleGroup;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final IExerciseRepository exerciseRepository;

    public List<ExerciseResponseDTO> findAll() {
        return exerciseRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void save(ExerciseRequestDTO exerciseRequestDTO) {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .name(exerciseRequestDTO.getName())
                .muscleGroup(exerciseRequestDTO.getMuscleGroup())
                .equipment(exerciseRequestDTO.getEquipment())
                .description(exerciseRequestDTO.getDescription())
                .difficultyLevel(
                        exerciseRequestDTO.getDifficultyLevel() != null
                                ? exerciseRequestDTO.getDifficultyLevel()
                                : DifficultyLevel.BEGINNER
                )
                .build();

        exerciseRepository.save(exercise);
    }

    public ExerciseResponseDTO getExerciseById(UUID id) throws NotFoundException {
        ExerciseEntity exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Exercise not found"));
        return toResponseDTO(exercise);
    }

    public List<ExerciseResponseDTO> getExerciseByMuscleGroup(String muscleGroup) throws BadRequestException {
        MuscleGroup group = parseMuscleGroup(muscleGroup);

        return exerciseRepository.findByMuscleGroup(group).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ExerciseResponseDTO updateExercise(UUID id, UpdateExerciseRequestDTO dto) throws NotFoundException {
        ExerciseEntity exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Exercise not found"));

        if (dto.getName() != null) exercise.setName(dto.getName());
        if (dto.getMuscleGroup() != null) exercise.setMuscleGroup(dto.getMuscleGroup());
        if (dto.getEquipment() != null) exercise.setEquipment(dto.getEquipment());
        if (dto.getDescription() != null) exercise.setDescription(dto.getDescription());
        if (dto.getDifficultyLevel() != null) exercise.setDifficultyLevel(dto.getDifficultyLevel());

        return toResponseDTO(exerciseRepository.save(exercise));
    }

    public void deleteExercise(UUID id) throws NotFoundException, BadRequestException {
        ExerciseEntity exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Exercise not found"));

        if (!exercise.getWorkouts().isEmpty()) {
            throw new BadRequestException("Cannot delete exercise because it is associated with one or more workouts");
        }

        exerciseRepository.deleteById(id);
    }

    private MuscleGroup parseMuscleGroup(String value) throws BadRequestException {
        try {
            return MuscleGroup.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid muscle group: " + value);
        }
    }

    private ExerciseResponseDTO toResponseDTO(ExerciseEntity exercise) {
        return ExerciseResponseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .muscleGroup(exercise.getMuscleGroup())
                .equipment(exercise.getEquipment())
                .description(exercise.getDescription())
                .difficultyLevel(exercise.getDifficultyLevel())
                .createdAt(exercise.getCreatedAt())
                .build();
    }
}