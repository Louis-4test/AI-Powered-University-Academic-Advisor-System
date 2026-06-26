package com.pecar.academic.service;

import com.pecar.academic.dto.AuthRequest;
import com.pecar.academic.dto.AuthResponse;
import com.pecar.academic.entity.Role;
import com.pecar.academic.entity.User;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.repository.UserRepository;
import com.pecar.academic.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("test@pecar.edu")
                .fullName("Test User").role(Role.STUDENT)
                .build();
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail("test@pecar.edu")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtUtils.generateToken("test@pecar.edu")).thenReturn("token");

        AuthResponse response = authService.register("test@pecar.edu", "password", "Test User", Role.STUDENT);
        assertNotNull(response);
        assertEquals("token", response.getToken());
        assertEquals("test@pecar.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
    }

    @Test
    void register_duplicateEmail() {
        when(userRepository.existsByEmail("test@pecar.edu")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> authService.register("test@pecar.edu", "password", "Test User", Role.STUDENT));
    }

    @Test
    void login_success() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@pecar.edu");
        request.setPassword("password");

        when(userRepository.findByEmail("test@pecar.edu")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("test@pecar.edu")).thenReturn("token");

        AuthResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
