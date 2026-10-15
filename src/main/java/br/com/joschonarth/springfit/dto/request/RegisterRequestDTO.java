package br.com.joschonarth.springfit.dto.request;

import br.com.joschonarth.springfit.validation.MaxBytes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for user registration")
public class RegisterRequestDTO {

    @Schema(description = "User full name", example = "John Doe")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "User email", example = "john@email.com")
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Schema(description = "User password", example = "secret123")
    @NotBlank
    @Size(min = 8, message = "Password must have at least 8 characters")
    @MaxBytes(value = 72, message = "Password must have at most 72 bytes in UTF-8")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one digit")
    private String password;

    @Schema(description = "User birth date", example = "2000-01-15")
    @Past
    private LocalDate birthDate;

    @Schema(description = "User phone number", example = "+55 11 99999-9999")
    @Pattern(regexp = "^\\+?[0-9()\\s-]{8,20}$", message = "Invalid phone number")
    private String phone;
}
