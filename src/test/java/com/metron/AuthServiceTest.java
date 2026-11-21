package com.metron;

import com.metron.dto.AuthResponse;
import com.metron.dto.LoginRequest;
import com.metron.dto.RegisterRequest;
import com.metron.exception.BusinessValidationException;
import com.metron.exception.UnauthorizedException;
import com.metron.model.entity.User;
import com.metron.repository.UserRepository;
import com.metron.security.JwtTokenProvider;
import com.metron.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Mariana Costa");
        registerRequest.setEmail("mariana@metron.com");
        registerRequest.setPassword("segredo123");
        registerRequest.setMonthlyIncome(new BigDecimal("4500.00"));
        registerRequest.setCurrentBankBalance(new BigDecimal("3000.00"));
        registerRequest.setSavingsGoalMonthly(new BigDecimal("900.00"));
        registerRequest.setSalaryDay(10);
    }

    @Test
    @DisplayName("Cadastro de usuário com sucesso e geração de token JWT")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("mariana@metron.com")).thenReturn(false);
        when(passwordEncoder.encode("segredo123")).thenReturn("$2a$10$hashedPasswordSample");

        User saved = new User();
        saved.setId(1L);
        saved.setName("Mariana Costa");
        saved.setEmail("mariana@metron.com");
        saved.setPasswordHash("$2a$10$hashedPasswordSample");
        saved.setMonthlyIncome(new BigDecimal("4500.00"));
        saved.setCurrentBankBalance(new BigDecimal("3000.00"));
        saved.setSavingsGoalMonthly(new BigDecimal("900.00"));
        saved.setSalaryDay(10);

        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(tokenProvider.generateToken("mariana@metron.com")).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUser().getEmail()).isEqualTo("mariana@metron.com");
        assertThat(response.getUser().getSalaryDay()).isEqualTo(10);
    }

    @Test
    @DisplayName("Rejeição de cadastro com e-mail duplicado")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("mariana@metron.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Já existe um usuário cadastrado com este e-mail");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login com sucesso quando credenciais são válidas")
    void testLoginSuccess() {
        User user = new User();
        user.setId(1L);
        user.setEmail("mariana@metron.com");
        user.setPasswordHash("$2a$10$hashedPasswordSample");

        when(userRepository.findByEmail("mariana@metron.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("segredo123", "$2a$10$hashedPasswordSample")).thenReturn(true);
        when(tokenProvider.generateToken("mariana@metron.com")).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(new LoginRequest("mariana@metron.com", "segredo123"));

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUser().getEmail()).isEqualTo("mariana@metron.com");
    }

    @Test
    @DisplayName("Rejeição de login com senha incorreta")
    void testLoginInvalidPassword() {
        User user = new User();
        user.setId(1L);
        user.setEmail("mariana@metron.com");
        user.setPasswordHash("$2a$10$hashedPasswordSample");

        when(userRepository.findByEmail("mariana@metron.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha_errada", "$2a$10$hashedPasswordSample")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("mariana@metron.com", "senha_errada")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("E-mail ou senha inválidos");
    }
}
