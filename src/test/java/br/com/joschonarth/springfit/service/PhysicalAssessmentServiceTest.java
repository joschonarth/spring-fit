package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.config.RabbitMQConfiguration;
import br.com.joschonarth.springfit.database.model.PhysicalAssessmentEntity;
import br.com.joschonarth.springfit.database.model.StudentEntity;
import br.com.joschonarth.springfit.database.repository.IPhysicalAssessmentRepository;
import br.com.joschonarth.springfit.database.repository.IStudentRepository;
import br.com.joschonarth.springfit.dto.event.PhysicalAssessmentCreatedEvent;
import br.com.joschonarth.springfit.dto.request.PhysicalAssessmentRequestDTO;
import br.com.joschonarth.springfit.enums.BmiClassification;
import br.com.joschonarth.springfit.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhysicalAssessmentServiceTest {

    @Mock
    private IStudentRepository studentRepository;

    @Mock
    private IPhysicalAssessmentRepository physicalAssessmentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PhysicalAssessmentService physicalAssessmentService;

    private StudentEntity student;

    @BeforeEach
    void setUp() {
        student = StudentEntity.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .build();
    }

    private PhysicalAssessmentRequestDTO requestOf(String weight, String height, String bodyFat) {
        return new PhysicalAssessmentRequestDTO(
                student.getId(),
                new BigDecimal(weight),
                new BigDecimal(height),
                new BigDecimal(bodyFat));
    }

    private void stubSaveAssigningGeneratedFields(UUID id, LocalDateTime createdAt) {
        when(physicalAssessmentRepository.save(any(PhysicalAssessmentEntity.class)))
                .thenAnswer(invocation -> {
                    PhysicalAssessmentEntity entity = invocation.getArgument(0);
                    entity.setId(id);
                    entity.setCreatedAt(createdAt);
                    return entity;
                });
    }

    @Nested
    @DisplayName("createPhysicalAssessment")
    class CreatePhysicalAssessment {

        @Test
        @DisplayName("should persist the assessment with BMI rounded HALF_UP to 2 decimals")
        void shouldPersistWithCalculatedBmi() throws NotFoundException {
            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            stubSaveAssigningGeneratedFields(UUID.randomUUID(), LocalDateTime.now());

            physicalAssessmentService.createPhysicalAssessment(requestOf("70", "1.75", "15.5"));

            ArgumentCaptor<PhysicalAssessmentEntity> captor = ArgumentCaptor.forClass(PhysicalAssessmentEntity.class);
            verify(physicalAssessmentRepository).save(captor.capture());
            PhysicalAssessmentEntity saved = captor.getValue();

            assertThat(saved.getBmi()).isEqualByComparingTo("22.86");
            assertThat(saved.getBmiClassification()).isEqualTo(BmiClassification.NORMAL);
            assertThat(saved.getWeight()).isEqualByComparingTo("70");
            assertThat(saved.getHeight()).isEqualByComparingTo("1.75");
            assertThat(saved.getBodyFatPercentage()).isEqualByComparingTo("15.5");
            assertThat(saved.getStudent()).isSameAs(student);
        }

        @ParameterizedTest(name = "BMI {0} should be classified as {1}")
        @CsvSource({
                "18.49, UNDERWEIGHT",
                "18.50, NORMAL",
                "24.95, NORMAL",
                "25.00, OVERWEIGHT",
                "29.95, OVERWEIGHT",
                "30.00, OBESE",
        })
        @DisplayName("should classify BMI correctly around each threshold")
        void shouldClassifyBmiAtBoundaries(String bmi, BmiClassification expected) throws NotFoundException {
            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            stubSaveAssigningGeneratedFields(UUID.randomUUID(), LocalDateTime.now());

            physicalAssessmentService.createPhysicalAssessment(requestOf(bmi, "1.00", "20"));

            ArgumentCaptor<PhysicalAssessmentEntity> captor = ArgumentCaptor.forClass(PhysicalAssessmentEntity.class);
            verify(physicalAssessmentRepository).save(captor.capture());
            assertThat(captor.getValue().getBmi()).isEqualByComparingTo(bmi);
            assertThat(captor.getValue().getBmiClassification()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should publish the event to the assessment exchange using the persisted data")
        void shouldPublishEventBuiltFromPersistedAssessment() throws NotFoundException {
            UUID assessmentId = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2026, 10, 19, 12, 0);

            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            stubSaveAssigningGeneratedFields(assessmentId, createdAt);

            physicalAssessmentService.createPhysicalAssessment(requestOf("70", "1.75", "15.5"));

            ArgumentCaptor<PhysicalAssessmentCreatedEvent> captor =
                    ArgumentCaptor.forClass(PhysicalAssessmentCreatedEvent.class);
            verify(rabbitTemplate).convertAndSend(
                    eq(RabbitMQConfiguration.ASSESSMENT_EXCHANGE),
                    eq(RabbitMQConfiguration.ASSESSMENT_CREATED_ROUTING_KEY),
                    captor.capture());

            assertThat(captor.getValue()).isEqualTo(new PhysicalAssessmentCreatedEvent(
                    assessmentId,
                    student.getId(),
                    "John Doe",
                    new BigDecimal("22.86"),
                    "NORMAL",
                    createdAt));
        }

        @Test
        @DisplayName("should throw NotFoundException and neither persist nor publish when the student does not exist")
        void shouldThrowWhenStudentNotFound() {
            when(studentRepository.findById(student.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> physicalAssessmentService.createPhysicalAssessment(requestOf("70", "1.75", "15")))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Student not found");

            verify(physicalAssessmentRepository, never()).save(any());
            verifyNoInteractions(rabbitTemplate);
        }

        @Test
        @DisplayName("should not publish an event when persisting fails")
        void shouldNotPublishWhenSaveFails() {
            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(physicalAssessmentRepository.save(any(PhysicalAssessmentEntity.class)))
                    .thenThrow(new IllegalStateException("db down"));

            assertThatThrownBy(() -> physicalAssessmentService.createPhysicalAssessment(requestOf("70", "1.75", "15")))
                    .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(rabbitTemplate);
        }
    }
}