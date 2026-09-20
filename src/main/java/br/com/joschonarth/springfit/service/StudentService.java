package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.PhysicalAssessmentEntity;
import br.com.joschonarth.springfit.database.model.StudentEntity;
import br.com.joschonarth.springfit.database.model.WorkoutEntity;
import br.com.joschonarth.springfit.database.repository.IPhysicalAssessmentRepository;
import br.com.joschonarth.springfit.database.repository.IStudentRepository;
import br.com.joschonarth.springfit.database.repository.IWorkoutRepository;
import br.com.joschonarth.springfit.dto.response.PhysicalAssessmentResponseDTO;
import br.com.joschonarth.springfit.dto.response.StudentResponseDTO;
import br.com.joschonarth.springfit.dto.request.UpdateStudentRequestDTO;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final IPhysicalAssessmentRepository physicalAssessmentRepository;
    private final IWorkoutRepository workoutRepository;
    private final IStudentRepository studentRepository;

    public PhysicalAssessmentResponseDTO getStudentAssessment(UUID studentId, UUID assessmentId) throws NotFoundException {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        PhysicalAssessmentEntity assessment = physicalAssessmentRepository.findByIdAndStudentId(assessmentId, studentId)
                .orElseThrow(() -> new NotFoundException("Physical Assessment not found for this student"));

        return toAssessmentResponseDTO(assessment);
    }

    public List<PhysicalAssessmentResponseDTO> getStudentAssessments(UUID studentId) throws NotFoundException {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        return physicalAssessmentRepository.findAllByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toAssessmentResponseDTO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(UUID studentId)throws NotFoundException {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        List<UUID> workoutIds = student.getWorkouts().stream()
                .map(WorkoutEntity::getId)
                .toList();

        workoutRepository.deleteAllById(workoutIds);

        physicalAssessmentRepository.deleteAllByStudentId(studentId);

        studentRepository.deleteById(studentId);
    }

    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public StudentResponseDTO getStudentById(UUID studentId) throws NotFoundException {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        return toResponseDTO(student);
    }

    public StudentResponseDTO updateStudent(UUID studentId, UpdateStudentRequestDTO dto) throws NotFoundException {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        if (dto.getName() != null) student.setName(dto.getName());
        if (dto.getPhone() != null) student.setPhone(dto.getPhone());
        if (dto.getBirthDate() != null) student.setBirthDate(dto.getBirthDate());

        return toResponseDTO(studentRepository.save(student));
    }

    private StudentResponseDTO toResponseDTO(StudentEntity student) {
        return StudentResponseDTO.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .birthDate(student.getBirthDate())
                .phone(student.getPhone())
                .createdAt(student.getCreatedAt())
                .build();
    }

    private PhysicalAssessmentResponseDTO toAssessmentResponseDTO(PhysicalAssessmentEntity assessment) {
        return PhysicalAssessmentResponseDTO.builder()
                .id(assessment.getId())
                .studentId(assessment.getStudent().getId())
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
