package br.com.joschonarth.springfit.database.repository;

import br.com.joschonarth.springfit.database.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface INotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);
}