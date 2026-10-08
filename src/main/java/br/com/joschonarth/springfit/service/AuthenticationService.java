package br.com.joschonarth.springfit.service;

import br.com.joschonarth.springfit.config.TokenProvider;
import br.com.joschonarth.springfit.database.model.RefreshTokenEntity;
import br.com.joschonarth.springfit.database.model.RolesEntity;
import br.com.joschonarth.springfit.database.model.StudentEntity;
import br.com.joschonarth.springfit.database.repository.IRefreshTokenRepository;
import br.com.joschonarth.springfit.database.repository.IRolesRepository;
import br.com.joschonarth.springfit.database.repository.IStudentRepository;
import br.com.joschonarth.springfit.dto.request.LoginRequestDTO;
import br.com.joschonarth.springfit.dto.request.RegisterRequestDTO;
import br.com.joschonarth.springfit.dto.response.RefreshTokenRequestDTO;
import br.com.joschonarth.springfit.dto.response.TokenResponseDTO;
import br.com.joschonarth.springfit.enums.RoleTypeEnum;
import br.com.joschonarth.springfit.exception.BadRequestException;
import br.com.joschonarth.springfit.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final IStudentRepository studentRepository;
    private final IRolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final IRefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationTime;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public void register(RegisterRequestDTO dto) throws BadRequestException {
        StudentEntity student = studentRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (student != null) {
            throw new BadRequestException("Student already registered with this email");
        }

        RolesEntity role = rolesRepository.findByName(RoleTypeEnum.ROLE_STUDENT.name())
                .orElseGet(() -> rolesRepository.save(RolesEntity.builder()
                        .name(RoleTypeEnum.ROLE_STUDENT.name())
                        .build()
                ));

        studentRepository.save(StudentEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .roles(Set.of(role))
                .password(passwordEncoder.encode(dto.getPassword()))
                .build());
    }

    public TokenResponseDTO login(LoginRequestDTO dto) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
            StudentEntity student = (StudentEntity) authentication.getPrincipal();

            String token = tokenProvider.generateToken(authentication);
            String refreshToken = createRefreshToken(student);

            return new TokenResponseDTO(token, refreshToken, expirationTime);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Transactional
    public TokenResponseDTO refresh(RefreshTokenRequestDTO dto) throws UnauthorizedException {
        RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(hash(dto.getRefreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        StudentEntity student = stored.getStudent();

        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllByStudentId(student.getId());
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        stored.setRevoked(true);

        return new TokenResponseDTO(
                tokenProvider.generateToken(student.getUsername()),
                createRefreshToken(student),
                expirationTime
        );
    }

    @Transactional
    public void logout(RefreshTokenRequestDTO dto) {
        refreshTokenRepository.findByTokenHash(hash(dto.getRefreshToken()))
                .ifPresent(t -> t.setRevoked(true));
    }

    private String createRefreshToken(StudentEntity student) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .tokenHash(hash(raw))
                .student(student)
                .expiresAt(Instant.now().plusMillis(refreshExpirationTime))
                .build());

        return raw;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
