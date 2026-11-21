package com.metron.security;

import com.metron.exception.UnauthorizedException;
import com.metron.model.entity.User;
import com.metron.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    private final UserRepository userRepository;

    public CurrentUserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Usuário não autenticado. Faça login para continuar.");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuário da sessão não encontrado no sistema."));
    }
}
