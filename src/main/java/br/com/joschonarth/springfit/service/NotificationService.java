package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.repository.INotificationRepository;
import br.com.joschonarth.springfit.dto.response.NotificationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getStudentNotifications(UUID studentId) {
        return notificationRepository.findAllByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(n -> new NotificationResponseDTO(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .toList();
    }
}