package br.com.joschonarth.springfit.listener;

import br.com.joschonarth.springfit.config.RabbitMQConfiguration;
import br.com.joschonarth.springfit.database.model.NotificationEntity;
import br.com.joschonarth.springfit.database.repository.INotificationRepository;
import br.com.joschonarth.springfit.dto.event.PhysicalAssessmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhysicalAssessmentNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PhysicalAssessmentNotificationListener.class);

    private final INotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfiguration.ASSESSMENT_CREATED_QUEUE)
    public void handle(PhysicalAssessmentCreatedEvent event) {
        String message = String.format(
                "Your new physical assessment has been recorded. BMI: %s (%s)",
                event.bmi(), event.bmiClassification()
        );

        notificationRepository.save(NotificationEntity.builder()
                .studentId(event.studentId())
                .message(message)
                .read(false)
                .build());

        log.info("Notification created for student {}: {}", event.studentId(), message);
    }
}