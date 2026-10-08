package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.database.model.NotificationEntity;
import br.com.joschonarth.springfit.database.repository.INotificationRepository;
import br.com.joschonarth.springfit.dto.response.NotificationResponseDTO;
import br.com.joschonarth.springfit.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private INotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID studentId;
    private UUID notificationId;
    private NotificationEntity notification;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        notification = NotificationEntity.builder()
                .id(notificationId)
                .studentId(studentId)
                .message("Your new physical assessment has been recorded. BMI: 23.55 (NORMAL)")
                .read(false)
                .createdAt(LocalDateTime.of(2026, 9, 27, 21, 16, 47))
                .build();
    }

    @Nested
    @DisplayName("getStudentNotifications")
    class GetStudentNotifications {

        @Test
        @DisplayName("should return notifications mapped to response DTO")
        void shouldReturnNotificationsMappedToDTO() {
            when(notificationRepository.findAllByStudentIdOrderByCreatedAtDesc(studentId))
                    .thenReturn(List.of(notification));

            List<NotificationResponseDTO> result = notificationService.getStudentNotifications(studentId);

            assertThat(result).hasSize(1);
            NotificationResponseDTO dto = result.get(0);
            assertThat(dto.id()).isEqualTo(notificationId);
            assertThat(dto.message()).isEqualTo(notification.getMessage());
            assertThat(dto.read()).isFalse();
            assertThat(dto.createdAt()).isEqualTo(notification.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("should mark notification as read and save it")
        void shouldMarkNotificationAsRead() throws NotFoundException {
            when(notificationRepository.findByIdAndStudentId(notificationId, studentId))
                    .thenReturn(Optional.of(notification));

            notificationService.markAsRead(studentId, notificationId);

            assertThat(notification.isRead()).isTrue();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("should throw NotFoundException when notification does not exist for the student")
        void shouldThrowWhenNotificationNotFound() {
            when(notificationRepository.findByIdAndStudentId(notificationId, studentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(studentId, notificationId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Notification not found");

            verify(notificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsRead {

        @Test
        @DisplayName("should mark all unread notifications as read and save them")
        void shouldMarkAllUnreadNotificationsAsRead() {
            NotificationEntity second = NotificationEntity.builder()
                    .id(UUID.randomUUID())
                    .studentId(studentId)
                    .message("Second notification")
                    .read(false)
                    .createdAt(LocalDateTime.of(2026, 9, 28, 10, 0))
                    .build();
            List<NotificationEntity> unread = List.of(notification, second);

            when(notificationRepository.findAllByStudentIdAndReadFalse(studentId)).thenReturn(unread);

            notificationService.markAllAsRead(studentId);

            assertThat(unread).allMatch(NotificationEntity::isRead);
            verify(notificationRepository).saveAll(unread);
        }
    }
}