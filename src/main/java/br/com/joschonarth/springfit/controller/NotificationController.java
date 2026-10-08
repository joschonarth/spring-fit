package br.com.joschonarth.springfit.controller;

import br.com.joschonarth.springfit.dto.response.NotificationResponseDTO;
import br.com.joschonarth.springfit.exception.NotFoundException;
import br.com.joschonarth.springfit.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notification", description = "Endpoints for student notifications")
@RestController
@RequestMapping("v1/student/{studentId}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List notifications for a student")
    @PreAuthorize("#studentId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping
    public List<NotificationResponseDTO> getStudentNotifications(@PathVariable UUID studentId) {
        return notificationService.getStudentNotifications(studentId);
    }

    @Operation(summary = "Mark a notification as read")
    @PreAuthorize("#studentId == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping("{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @PathVariable UUID studentId,
            @PathVariable UUID notificationId) throws NotFoundException {
        notificationService.markAsRead(studentId, notificationId);
    }

    @Operation(summary = "Mark all notifications as read")
    @PreAuthorize("#studentId == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping("read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@PathVariable UUID studentId) {
        notificationService.markAllAsRead(studentId);
    }
}