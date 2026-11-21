package com.metron.controller;

import com.metron.dto.UpdateProfileRequest;
import com.metron.dto.UserDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CurrentUserContext userContext;
    private final AuthService authService;

    public UserController(CurrentUserContext userContext, AuthService authService) {
        this.userContext = userContext;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(new UserDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = userContext.getCurrentUser();
        UserDto updated = authService.updateProfile(user, request);
        return ResponseEntity.ok(updated);
    }
}
