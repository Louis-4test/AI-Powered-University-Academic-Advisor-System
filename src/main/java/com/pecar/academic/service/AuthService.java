package com.pecar.academic.service;

import com.pecar.academic.dto.AuthRequest;
import com.pecar.academic.dto.AuthResponse;
import com.pecar.academic.entity.Role;
import com.pecar.academic.entity.User;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.UserRepository;
import com.pecar.academic.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository          userRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtUtils                jwtUtils;
    private final AuthenticationManager   authenticationManager;
    private final EmailService            emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public AuthResponse register(String email, String rawPassword, String fullName, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A user with email " + email + " already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role(role)
                .build();
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        String token = jwtUtils.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public Map<String, String> generateResetToken(String email) {
        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Map.of("message", "If the email exists, a reset link has been sent.");
        }

        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetUrl);

        log.info("Password reset token generated for: {} – {}", email, resetUrl);

        return Map.of("message", "If the email exists, a reset link has been sent.");
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new ResourceNotFoundException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successfully for: {}", user.getEmail());
    }
}
