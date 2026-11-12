package br.com.joschonarth.springfit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User response data")
public class UserResponseDTO {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User full name", example = "John Doe")
    private String name;

    @Schema(description = "User email", example = "john@email.com")
    private String email;

    @Schema(description = "User birth date", example = "2000-01-15")
    private LocalDate birthDate;

    @Schema(description = "User phone number", example = "+55 11 99999-9999")
    private String phone;

    @Schema(description = "Registration date")
    private LocalDateTime createdAt;
}