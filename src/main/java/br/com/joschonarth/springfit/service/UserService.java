package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.PhysicalAssessmentEntity;
import br.com.joschonarth.springfit.database.model.UserEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IPhysicalAssessmentRepository;
import br.com.joschonarth.springfit.database.repository.IUserRepository;
import br.com.joschonarth.springfit.database.repository.IWorkoutRepository;
import br.com.joschonarth.springfit.dto.response.PhysicalAssessmentResponseDTO;
import br.com.joschonarth.springfit.dto.response.UserResponseDTO;
import br.com.joschonarth.springfit.dto.request.UpdateUserRequestDTO;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IPhysicalAssessmentRepository physicalAssessmentRepository;
    private final IWorkoutRepository workoutRepository;
    private final IUserRepository userRepository;

    public PhysicalAssessmentResponseDTO getUserAssessment(UUID userId, UUID assessmentId) throws NotFoundException {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PhysicalAssessmentEntity assessment = physicalAssessmentRepository.findByIdAndUserId(assessmentId, userId)
                .orElseThrow(() -> new NotFoundException("Physical Assessment not found for this user"));

        return toAssessmentResponseDTO(assessment);
    }

    public List<PhysicalAssessmentResponseDTO> getUserAssessments(UUID userId) throws NotFoundException {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return physicalAssessmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toAssessmentResponseDTO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(UUID userId) throws NotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<UUID> workoutIds = user.getWorkouts().stream()
                .map(WorkoutEntity::getId)
                .toList();

        workoutRepository.deleteAllById(workoutIds);
        physicalAssessmentRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UserResponseDTO getUserById(UUID userId) throws NotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return toResponseDTO(user);
    }

    public UserResponseDTO updateUser(UUID userId, UpdateUserRequestDTO dto) throws NotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());

        return toResponseDTO(userRepository.save(user));
    }

    private UserResponseDTO toResponseDTO(UserEntity user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .birthDate(user.getBirthDate())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PhysicalAssessmentResponseDTO toAssessmentResponseDTO(PhysicalAssessmentEntity assessment) {
        return PhysicalAssessmentResponseDTO.builder()
                .id(assessment.getId())
                .userId(assessment.getUser().getId())
                .weight(assessment.getWeight())
                .height(assessment.getHeight())
                .bodyFatPercentage(assessment.getBodyFatPercentage())
                .bmi(assessment.getBmi())
                .bmiClassification(assessment.getBmiClassification())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }
}
