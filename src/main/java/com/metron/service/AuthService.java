package com.metron.service;

import com.metron.dto.*;
import com.metron.exception.BusinessValidationException;
import com.metron.exception.ResourceNotFoundException;
import com.metron.exception.UnauthorizedException;
import com.metron.model.entity.User;
import com.metron.repository.UserRepository;
import com.metron.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BusinessValidationException("Já existe um usuário cadastrado com este e-mail.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setMonthlyIncome(request.getMonthlyIncome() != null ? request.getMonthlyIncome() : BigDecimal.ZERO);
        user.setCurrentBankBalance(request.getCurrentBankBalance() != null ? request.getCurrentBankBalance() : BigDecimal.ZERO);
        user.setSavingsGoalMonthly(request.getSavingsGoalMonthly() != null ? request.getSavingsGoalMonthly() : BigDecimal.ZERO);
        user.setSalaryDay(request.getSalaryDay() != null ? request.getSalaryDay() : 5);

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser.getEmail());

        return new AuthResponse(token, new UserDto(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }

        String token = tokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, new UserDto(user));
    }

    @Transactional
    public UserDto updateProfile(User currentUser, UpdateProfileRequest request) {
        currentUser.setName(request.getName().trim());

        if (request.getMonthlyIncome() != null) {
            currentUser.setMonthlyIncome(request.getMonthlyIncome());
        }
        if (request.getCurrentBankBalance() != null) {
            currentUser.setCurrentBankBalance(request.getCurrentBankBalance());
        }
        if (request.getSavingsGoalMonthly() != null) {
            currentUser.setSavingsGoalMonthly(request.getSavingsGoalMonthly());
        }
        if (request.getSalaryDay() != null) {
            if (request.getSalaryDay() < 1 || request.getSalaryDay() > 31) {
                throw new BusinessValidationException("O dia de recebimento deve ser entre 1 e 31.");
            }
            currentUser.setSalaryDay(request.getSalaryDay());
        }

        // Password change if requested
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
                throw new BusinessValidationException("A senha atual informada está incorreta.");
            }
            if (request.getNewPassword().length() < 6) {
                throw new BusinessValidationException("A nova senha deve ter no mínimo 6 caracteres.");
            }
            currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        User saved = userRepository.save(currentUser);
        return new UserDto(saved);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para o e-mail informado."));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
