package br.com.joschonarth.springfit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for updating a user")
public class UpdateUserRequestDTO {

    @Schema(description = "User full name", example = "John Doe")
    private String name;

    @Schema(description = "User phone number", example = "+55 11 99999-9999")
    private String phone;

    @Schema(description = "User birth date", example = "2000-01-15")
    private LocalDate birthDate;
}