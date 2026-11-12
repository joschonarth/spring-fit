package br.com.joschonarth.springfit.database.repository;

import br.com.joschonarth.springfit.database.model.PhysicalAssessmentEntity;
import br.com.joschonarth.springfit.dto.projection.PhysicalAssessmentProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPhysicalAssessmentRepository extends JpaRepository<PhysicalAssessmentEntity, UUID> {

    @Query("""
        SELECT u.id AS userId,
               u.name AS userName,
               pa.id AS assessmentId,
               pa.weight AS weight,
               pa.height AS height,
               pa.bodyFatPercentage AS bodyFatPercentage,
               pa.bmi AS bmi,
               pa.bmiClassification AS bmiClassification,
               pa.createdAt AS createdAt,
               pa.updatedAt AS updatedAt
        FROM PhysicalAssessmentEntity pa
        JOIN pa.user u
        """)
    List<PhysicalAssessmentProjection> getAllAssessments();

    @Query(value = """
        SELECT u.id AS userId,
               u.name AS userName,
               pa.id AS assessmentId,
               pa.weight AS weight,
               pa.height AS height,
               pa.bodyFatPercentage AS bodyFatPercentage,
               pa.bmi AS bmi,
               pa.bmiClassification AS bmiClassification,
               pa.createdAt AS createdAt,
               pa.updatedAt AS updatedAt
        FROM PhysicalAssessmentEntity pa
        JOIN pa.user u
        """,
    countQuery = """
        SELECT count(pa.id)
        FROM PhysicalAssessmentEntity pa
        JOIN pa.user u
        """)
    Page<PhysicalAssessmentProjection> getAllAssessmentsPage(Pageable pageable);

    Optional<PhysicalAssessmentEntity> findByIdAndUserId(UUID assessmentId, UUID userId);

    List<PhysicalAssessmentEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteAllByUserId(UUID userId);
}
