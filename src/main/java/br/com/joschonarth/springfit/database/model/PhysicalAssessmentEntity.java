package br.com.joschonarth.springfit.database.model;

import br.com.joschonarth.springfit.enums.BmiClassification;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "physical_assessment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class PhysicalAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal weight;

    @Column(nullable = false)
    private BigDecimal height;

    @Column(name = "body_fat_percentage", nullable = false)
    private BigDecimal bodyFatPercentage;

    @Column(nullable = false)
    private BigDecimal bmi;

    @Enumerated(EnumType.STRING)
    @Column(name = "bmi_classification")
    private BmiClassification bmiClassification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
