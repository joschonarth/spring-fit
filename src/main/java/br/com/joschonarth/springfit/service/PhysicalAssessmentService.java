package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.PhysicalAssessmentEntity;
import br.com.joschonarth.springfit.database.model.StudentEntity;
import br.com.joschonarth.springfit.database.repository.IPhysicalAssessmentRepository;
import br.com.joschonarth.springfit.database.repository.IStudentRepository;
import br.com.joschonarth.springfit.dto.request.PhysicalAssessmentRequestDTO;
import br.com.joschonarth.springfit.dto.projection.PhysicalAssessmentProjection;
import br.com.joschonarth.springfit.enums.BmiClassification;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhysicalAssessmentService {

    private final IStudentRepository studentRepository;
    private final IPhysicalAssessmentRepository physicalAssessmentRepository;

    public void createPhysicalAssessment(PhysicalAssessmentRequestDTO dto) throws NotFoundException {
        StudentEntity student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));

        BigDecimal bmi = dto.getWeight()
                .divide(dto.getHeight().multiply(dto.getHeight()), 2, RoundingMode.HALF_UP);

        PhysicalAssessmentEntity physicalAssessment = PhysicalAssessmentEntity.builder()
                .weight(dto.getWeight())
                .height(dto.getHeight())
                .bodyFatPercentage(dto.getBodyFatPercentage())
                .bmi(bmi)
                .bmiClassification(calculateBmiClassification(bmi))
                .student(student)
                .build();

        physicalAssessmentRepository.save(physicalAssessment);
    }

    public List<PhysicalAssessmentProjection> getAllAssessments() {
        return physicalAssessmentRepository.getAllAssessments();
    }

    public Page<PhysicalAssessmentProjection> getAllAssessmentsPageable(Integer page, Integer size) {
        return physicalAssessmentRepository.getAllAssessmentsPage(PageRequest.of(page, size));
    }

    private BmiClassification calculateBmiClassification(BigDecimal bmi) {
        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            return BmiClassification.UNDERWEIGHT;
        } else if (bmi.compareTo(new BigDecimal("24.9")) <= 0) {
            return BmiClassification.NORMAL;
        } else if (bmi.compareTo(new BigDecimal("29.9")) <= 0) {
            return BmiClassification.OVERWEIGHT;
        } else {
            return BmiClassification.OBESE;
        }
    }
}
