package br.com.joschonarth.springfit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for user login")
public class LoginRequestDTO {

    @Schema(description = "User email", example = "john@email.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "User password", example = "secret123")
    @NotBlank
    private String password;
}
