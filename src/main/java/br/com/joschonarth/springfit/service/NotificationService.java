package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.NotificationEntity;
import br.com.joschonarth.springfit.database.repository.INotificationRepository;
import br.com.joschonarth.springfit.dto.response.NotificationResponseDTO;
import br.com.joschonarth.springfit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getUserNotifications(UUID userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> new NotificationResponseDTO(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .toList();
    }

    public void markAsRead(UUID userId, UUID notificationId) throws NotFoundException {
        NotificationEntity notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<NotificationEntity> notifications = notificationRepository.findAllByUserIdAndReadFalse(userId);

        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}