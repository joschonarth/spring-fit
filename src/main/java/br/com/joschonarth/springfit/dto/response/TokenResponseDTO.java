package br.com.joschonarth.springfit.dto.response;

public record TokenResponseDTO(String token, String refreshToken, long expiresIn) {
}
