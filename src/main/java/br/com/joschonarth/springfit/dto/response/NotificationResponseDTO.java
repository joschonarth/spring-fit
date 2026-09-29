package br.com.joschonarth.springfit.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        String message,
        boolean read,
        LocalDateTime createdAt
) {}