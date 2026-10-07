package br.com.joschonarth.springfit.controller;

import br.com.joschonarth.springfit.dto.request.LoginRequestDTO;
import br.com.joschonarth.springfit.dto.request.RegisterRequestDTO;
import br.com.joschonarth.springfit.dto.response.RefreshTokenRequestDTO;
import br.com.joschonarth.springfit.dto.response.TokenResponseDTO;
import br.com.joschonarth.springfit.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Endpoints for user authentication")
@RestController
@RequestMapping("v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Email already in use or invalid data")
    })
    @PostMapping("register")
    public void register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) throws Exception {
        authenticationService.register(registerRequestDTO);
    }

    @Operation(summary = "Authenticate user and return JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PostMapping("login")
    public TokenResponseDTO login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) throws Exception {
        return authenticationService.login(loginRequestDTO);
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("refresh")
    public TokenResponseDTO refresh(@RequestBody @Valid RefreshTokenRequestDTO dto) throws Exception {
        return authenticationService.refresh(dto);
    }

    @Operation(summary = "Logout (revoke refresh token)")
    @PostMapping("logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        authenticationService.logout(dto);
    }
}
